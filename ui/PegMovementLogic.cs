using UnityEngine;
using System.Collections;

public class PegMovementLogic : MonoBehaviour
{
    [Header("Movement Settings")]
    public float jumpHeight = 1.5f;
    public float jumpSpeed = 5.0f;
    public float slickSlideSpeed = 8.0f; // Faster speed for the slide effect

    [Header("Game State")]
    public bool isMoving = false;

    // Reference to the Board Manager to check hole types
    private BoardManager boardManager;

    void Start()
    {
        boardManager = FindObjectOfType<BoardManager>();
    }

    /// <summary>
    /// Attempt to move a Peg from StartHole to TargetHole
    /// </summary>
    public void AttemptMove(Peg currentPeg, Hole startHole, Hole targetHole)
    {
        if (isMoving) return;

        // 1. Calculate the Middle Hole (the one being jumped over)
        Hole middleHole = boardManager.GetHoleBetween(startHole, targetHole);

        // 2. Validate Standard Jump
        if (middleHole != null && middleHole.HasPeg && !targetHole.HasPeg)
        {
            StartCoroutine(ExecuteJumpSequence(currentPeg, startHole, targetHole, middleHole));
        }
        else
        {
            Debug.Log("Invalid Move: Must jump over a peg into an empty hole.");
            // Trigger "Invalid Move" Shake Animation here
        }
    }

    private IEnumerator ExecuteJumpSequence(Peg peg, Hole start, Hole end, Hole jumped)
    {
        isMoving = true;

        // -- PHASE 1: THE JUMP --
        // Remove the jumped peg immediately or wait for apex? 
        // For "Visceral" feel, remove it when the jumping peg lands on it conceptually.
        Peg victimPeg = jumped.GetPeg();
        
        // Animate the Arc
        yield return StartCoroutine(AnimateArc(peg.transform, start.transform.position, end.transform.position));

        // Kill the jumped peg (Trigger Splat Sound/Particle here)
        victimPeg.DestroyPeg(); 
        jumped.ClearPeg();
        
        // Update Peg Logical Position
        peg.CurrentHole = end;
        end.SetPeg(peg);
        start.ClearPeg();

        // -- PHASE 2: THE SLICK SURFACE CHECK (Ooze World Logic) --
        if (end.holeType == HoleType.SlickOoze)
        {
            // Calculate the slide direction vector based on the jump
            Vector2Int direction = end.gridPosition - start.gridPosition;
            
            // Calculate potential slide target (1 unit further in same direction)
            Hole slideTarget = boardManager.GetHoleAt(end.gridPosition + direction);

            // Check if slide target exists and is empty
            if (slideTarget != null && !slideTarget.HasPeg)
            {
                Debug.Log("Hit Slick Surface! Sliding...");
                yield return StartCoroutine(AnimateSlide(peg.transform, end.transform.position, slideTarget.transform.position));

                // Update Logical Position after slide
                end.ClearPeg();
                peg.CurrentHole = slideTarget;
                slideTarget.SetPeg(peg);
            }
            else
            {
                // Visual feedback: Peg tries to slide but hits a wall/peg
                // Trigger "Thud" sound and small wobble
            }
        }

        isMoving = false;
        
        // Check Win Condition via GameManager
        FindObjectOfType<GameManager>().CheckGameState();
    }

    // Mathematical Arc for the jump (Parabola)
    private IEnumerator AnimateArc(Transform pegObj, Vector3 startPos, Vector3 endPos)
    {
        float journey = 0f;
        while (journey <= 1f)
        {
            journey += Time.deltaTime * jumpSpeed;
            
            // Linear interpolation for X/Z
            Vector3 currentPos = Vector3.Lerp(startPos, endPos, journey);
            
            // Parabolic interpolation for Y (Height)
            // 4 * height * x * (1 - x) creates a 0-1-0 arc
            currentPos.y += 4.0f * jumpHeight * journey * (1.0f - journey);
            
            pegObj.position = currentPos;
            yield return null;
        }
    }

    // Linear, fast slide for the Ooze effect
    private IEnumerator AnimateSlide(Transform pegObj, Vector3 startPos, Vector3 endPos)
    {
        float journey = 0f;
        while (journey <= 1f)
        {
            journey += Time.deltaTime * slickSlideSpeed; // Faster than jump
            pegObj.position = Vector3.Lerp(startPos, endPos, journey);
            yield return null;
        }
    }
}