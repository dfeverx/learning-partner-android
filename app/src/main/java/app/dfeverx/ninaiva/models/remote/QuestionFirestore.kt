package app.dfeverx.ninaiva.models.remote


class QuestionFirestore(
    val sm: String = "",
    val ops: List<OptionFirestore> = listOf(),
    val ex: String = "",
    val areaId: Int = 0
)
