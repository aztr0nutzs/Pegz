package com.neon.peggame.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.games.AchievementsClient
import com.google.android.gms.games.Games
import com.google.android.gms.games.GamesSignInClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayGamesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "PlayGamesManager"
    
    // --- Achievement IDs (MUST REPLACE with your Play Console IDs) ---
    private val ACH_GENIUS = "CgkxNDk2NTE0NDI0NDUSAhAA"      // 1 Peg Left
    private val ACH_FOUR_PEGS = "CgkxNDk2NTE0NDI0NDUSAhAB"   // 4 or less Pegs Left
    private val ACH_MOVEMASTER = "CgkxNDk2NTE0NDI0NDUSAhAC" // 50 Moves in one game

    // --- Clients and State ---
    private lateinit var gamesSignInClient: GamesSignInClient
    private var achievementsClient: AchievementsClient? = null

    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()
    
    init {
        gamesSignInClient = Games.getGamesSignInClient(context)
        checkSignInStatus()
    }
    
    private fun checkSignInStatus() {
        gamesSignInClient.isAuthenticated.addOnCompleteListener { isAuthenticatedTask ->
            if (isAuthenticatedTask.isSuccessful && isAuthenticatedTask.result.isAuthenticated) {
                val account = GoogleSignIn.getLastSignedInAccount(context)
                onSignedIn(account)
            } else {
                _isSignedIn.value = false
            }
        }
    }
    
    private fun onSignedIn(account: GoogleSignInAccount?) {
        if (account == null) return
        achievementsClient = Games.getAchievementsClient(context, account)
        _isSignedIn.value = true
        Log.i(TAG, "Player signed in successfully.")
    }

    // --- Public Methods ---

    fun signIn(activity: Activity) {
        gamesSignInClient.signIn().addOnCompleteListener(activity) { signInResultTask ->
            if (signInResultTask.isSuccessful) {
                onSignedIn(signInResultTask.result.signInAccount)
            } else {
                Log.e(TAG, "Sign-in failed: ${signInResultTask.exception}")
                _isSignedIn.value = false
            }
        }
    }
    
    fun unlockAchievement(pegsLeft: Int, movesMade: Int) {
        if (!isSignedIn.value) return

        when (pegsLeft) {
            1 -> achievementsClient?.unlock(ACH_GENIUS)?.addOnSuccessListener { 
                Log.d(TAG, "Achievement ACH_GENIUS unlocked.")
            }
            in 2..4 -> achievementsClient?.unlock(ACH_FOUR_PEGS)
        }
        
        if (movesMade >= 50) {
            achievementsClient?.unlock(ACH_MOVEMASTER)
        }
    }
    
    fun showAchievements(activity: Activity) {
        if (!isSignedIn.value) {
            signIn(activity) 
            return
        }
        
        achievementsClient?.getAchievementsIntent()
            ?.addOnSuccessListener { intent ->
                activity.startActivityForResult(intent, 0)
            }
            ?.addOnFailureListener { e ->
                Log.e(TAG, "Failed to show achievements: $e")
            }
    }
}
