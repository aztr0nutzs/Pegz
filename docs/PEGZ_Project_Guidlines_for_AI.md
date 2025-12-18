PEGZ_Project_Guidelines_for_AI.md
I. Core Project Mandates (Must Be Adhered To)
Rule ID	Mandate	Application
P-01	Maintain Aesthetic Consistency (The "Vibe")	All new themes, characters, and concepts must maintain the "crazy looking," high-contrast, edgy, and stylized aesthetic established by the Bio Lab, Haunted, and Ooze themes. Avoid "safe," generic, or low-contrast designs.
P-02	Respect IP Boundary	When generating content inspired by existing media (e.g., Friday After Next), only capture the Vibe, Atmosphere, and Scene Mechanics. NEVER use direct character names, likenesses, or copyrighted audio/visual samples. Focus on generalized tropes (e.g., "Miscreant Santa," not "Crackhead Santa").
P-03	Prioritize Gameplay Mechanics	All creative decisions (themes, character designs) must first support or be supported by a unique gameplay mechanic (e.g., Slick Surfaces, Hot Commodity). Themes must not be purely cosmetic.
P-04	No Image Repetition	STRICTLY FORBIDDEN: If the user requests new images, the response must generate entirely new URLs and visual concepts based on the most recently approved theme/concept. Re-generating or linking to previous image concepts is a critical failure.

II. GDD Consistency & File Management
Rule ID	Mandate	Application
G-01	Maintain GDD Hierarchy	Always reference the GDD section and rule when making a change (e.g., "Updating GDD Section III, Level 10"). Do not mix GDD sections in a single response.
G-02	Use Dedicated File Names	Any complex output (GDD, detailed UI structure, etc.) should be presented with a file name header (e.g., ## PEGZ_GDD_Section_IV.md) to signify its standalone nature.
G-03	Use Tables for Structure	Use tables for all lists, comparisons, and structural outlines (PEGZ abilities, level names, etc.) for clarity and scannability (See current response formatting).

III. AI Interaction and Workflow Rules
Rule ID	Mandate	Application
A-01	Acknowledge and Validate	Always explicitly confirm the user's last request and validate that the response adheres to the core aesthetic (P-01) and mechanics (P-03).
A-02	Propose High-Value Next Step	Every response must conclude with a single, high-value, and logical next step based on the GDD structure (G-01) and project priority.
A-03	Self-Correction & Error Flagging	If a previous mistake is noted by the user, the AI must explicitly apologize, identify the violated rule (e.g., P-04), and immediately execute the correct action.

📁 GDD File Structure & Necessary Files
To build a successful game, we need to formalize our current work into the official GDD file structure.

1. GDD Core File (PEGZ_GDD_v1.0.md)
We have already outlined Sections I, II, III, and V. We need to formalize these and focus next on the crucial Monetization and Customization sections.

2. UI/UX Document (PEGZ_UX_Customization_Guide.md)
This will be the next document we create. It will focus specifically on how the player interacts with all the collectibles and customization options we designed (PEGZ sets, Board Skins, Abilities).

3. Asset List (PEGZ_Asset_List_v1.0.md)
This will track all the themed assets required for development (3D models, sound effects, particle effects).

4. Scripting/Logic Flow (PEGZ_Logic_Flow.md)
This will detail the exact scripting logic for the unique PEGZ abilities and board obstacles (e.g., the exact code logic for the "Slick Surface" sliding).

Based on the GDD structure (G-01) and the logical flow of development, the next logical and high-value step (A-02) is to proceed with detailing the Customization and Collection Menus (UI/UX), as this directly supports the collectible nature of the game (GDD Section IV).
