package app.dfeverx.ninaiva.models.remote

import app.dfeverx.ninaiva.models.CreditInfo
import app.dfeverx.ninaiva.models.SubscriptionInfo

class FunResponse(
    var statusCode: Int = 0,
    var error: String = "",
    var data: StudyNoteWithQuestionsFirestore = StudyNoteWithQuestionsFirestore(),
    var credit: CreditInfo = CreditInfo(),
    var subscription: SubscriptionInfo = SubscriptionInfo()
)


