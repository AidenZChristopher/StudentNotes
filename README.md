-- Game Description -- 

You are hired to investigate the fate of a missing spaceship crew. When you arrive, the ship is overrun with hostile bugs, forcing you to fight through infested corridors as you uncover the truth behind the crew’s disappearance.

-- Overview --

The ship is infested with aggressive alien bugs that actively hunt the player throughout the environment. Armed with a gun, the player must defend themselves while exploring tight, dangerous corridors.

A timer begins as soon as the player spawns, tracking total run time. Faster completion results in higher scores, turning every decision into a risk-reward choice under pressure.

Players earn points by defeating bugs and completing levels quickly, competing for a spot on the top 10 leaderboard. Player initials are displayed alongside their scores to encourage replayability and competition.

-- Key Features --
Character Animation
- The player cycles through three animations: idle, running, and shooting.

Sound Design
- Creepy sci-fi music and powerful gun sound effects create a tense and immersive atmosphere.

AI Pathfinding Enemies
- Enemies actively search for the player and react dynamically to both sight and sound.

Destructible Environment 
- Breakable crates block paths and can be destroyed or used strategically for cover and positioning. A visible timer pressures the player to move quickly, with extra points awarded based on remaining time.

- Timed Run System 
When a level ends, it restarts with more enemies. Enemy counts scale based on the player’s score, creating progressively harder runs.

-- Technical Components --
Sound
- Audio files are stored as WAV files and loaded and played using SDL_mixer.

Physics
- Objects use Box2D for physics simulation, giving the world realistic movement and collision behavior.

Input System
- Inputs are stored in a two-frame key state system and read by movement and control components.

Graphics
- Graphics are rendered using SDL2. Object properties are stored in XML files and instantiated through a component-based system.

AI Systems
- Enemies exhibit intelligent behavior by reacting when they see the player or hear gunshots, interrupting patrol patterns to investigate.

-- Screenshots --
<img width="398" height="818" alt="Screenshot 2025-12-08 163741" src="https://github.com/user-attachments/assets/16213bcc-1d06-4b5f-9f17-60058ee9fdcf" />
<img width="398" height="818" alt="Screenshot 2025-12-08 163741" src="https://github.com/user-attachments/assets/9e64657a-2932-4067-a6f2-36f5157afd05" />
<img width="392" height="824" alt="Screenshot 2025-12-08 163810" src="https://github.com/user-attachments/assets/035ab9d8-d6ba-4dd5-a3d7-9c10c51dbce8" />
<img width="421" height="828" alt="Screenshot 2025-12-08 163828" src="https://github.com/user-attachments/assets/1881d7b6-3547-4e76-9dea-bbf91835d685" />
<img width="396" height="829" alt="Screenshot 2025-12-08 163838" src="https://github.com/user-attachments/assets/aed4b815-a2de-40f7-bea4-4173a42752a9" />
