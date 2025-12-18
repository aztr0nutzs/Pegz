object LevelRepository {

    val levels = listOf(
        LevelDefinition(
            id = 1,
            name = "Neon Bio-Lab",
            contaminated = listOf(Position(1,2), Position(3,2)),
            slick = listOf(Position(2,1)),
            timed = false
        ),
        LevelDefinition(
            id = 2,
            name = "Steamy Jungle",
            contaminated = listOf(Position(0,2), Position(4,2)),
            slick = listOf(Position(2,3)),
            timed = true
        )
    )
}
