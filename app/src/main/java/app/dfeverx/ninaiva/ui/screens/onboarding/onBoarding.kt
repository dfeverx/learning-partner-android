package app.dfeverx.ninaiva.ui.screens.onboarding

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.dfeverx.ninaiva.R
import app.dfeverx.ninaiva.ui.Screens
import app.dfeverx.ninaiva.ui.components.ContinueWithGoogleButton
import app.dfeverx.ninaiva.ui.components.TextProgressButton
import app.dfeverx.ninaiva.ui.main.MainViewModel
import app.dfeverx.ninaiva.utils.activityViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


@OptIn(ExperimentalPagerApi::class)
@Composable
fun Onboarding(navController: NavHostController) {
    val mainViewModel = activityViewModel<MainViewModel>()
    val auth = Firebase.auth
    val items = ArrayList<OnBoardingData>()
    items.add(
        OnBoardingData(
            R.drawable.illu_reading_a_book,
            "Scan and Summarize Instantly",
            "Digitize notes, generate key points instantly.",
            backgroundColor = Color(0x1A0189C5),
            mainColor = Color(0xFF00B5EA)
        )
    )

    items.add(
        OnBoardingData(
            R.drawable.illu_checklist,
            "Smart Questions, Easy Learning",
            "Generate questions, master topics effortlessly.",
            backgroundColor = Color(0x1AE4AF19),
            mainColor = Color(0xFFE4AF19)
        )
    )

    items.add(
        OnBoardingData(
            R.drawable.illu_calender_todo,
            "Retain with Spaced Repetition",
            "Optimize learning, enhance memory retention effectively.",
            backgroundColor = Color(0x4D78C155),
            mainColor = Color(0xFF78C155)
        )
    )
    items.add(
        OnBoardingData(
            R.drawable.illu_progress_chart,
            "Streaks for Consistent Progress",
            "Stay motivated, keep your learning streaks strong.",
            backgroundColor = Color(0x4D78C155),
            mainColor = Color(0xFF78C155)
        )
    )


    val pagerState = rememberPagerState(
        pageCount = items.size,
        initialOffscreenLimit = 2,
        infiniteLoop = false,
        initialPage = 0,
    )

    Scaffold(modifier = Modifier,
        content = {
            Log.d("TAG", "Onboarding: $it")
            Column(
                modifier = Modifier/*.padding(it)*/,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalPager(state = pagerState) { page ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(items[page].backgroundColor),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceAround
                    ) {

                        Image(
                            painter = painterResource(id = items[page].image),
                            contentDescription = items[page].title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 86.dp, bottom = 250.dp)
                        )


                    }
                }

            }
        }, bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {


                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .animateContentSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PagerIndicator(items = items, currentPage = pagerState.currentPage)
                    Text(
                        text = items[pagerState.currentPage].title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, end = 30.dp, start = 46.dp),
//                            color = Color(0xFF292D32),
                        color = items[pagerState.currentPage].mainColor,
//                            fontFamily = Poppins,
                        textAlign = TextAlign.Center,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold, minLines = 2
                    )

                    Text(
                        text = items[pagerState.currentPage].desc,
                        minLines = 3,
                        modifier = Modifier.padding(top = 8.dp, start = 40.dp, end = 20.dp),
                        color = Color.Gray,
//                            fontFamily = Poppins,
                        fontSize = 21.sp,
//                            textAlign = TextAlign.Center,
                        fontWeight = FontWeight.ExtraLight,
                        textAlign = TextAlign.Center
                    )

                    var isLoadingAnonymousAuth by rememberSaveable { mutableStateOf(false) }
                    var isLoadingGoogleAuth by rememberSaveable { mutableStateOf(false) }

                    ContinueWithGoogleButton(
                        modifier = Modifier.padding(top = 8.dp),
                        enabled = !isLoadingGoogleAuth,
                        onSuccess = {
                            mainViewModel.dataSyncOnGoogleAuthSuccessAndMarkingInitialFetch(it)
                            navController.popBackStack()
                            navController.navigate(Screens.Home.route)
                        },
                        onFailure = {
//                                    Todo:Error message
                        },
                        updateLoading = {
                            isLoadingGoogleAuth = it
                        }
                    )
                    TextProgressButton(
                        modifier = Modifier.padding(16.dp),
                        isLoading = isLoadingAnonymousAuth,
                        onClick = {
                            isLoadingAnonymousAuth = true
                            auth.signInAnonymously()
                                .addOnSuccessListener {
                                    navController.popBackStack()
                                    navController.navigate(Screens.Home.route)
                                    isLoadingAnonymousAuth = false
                                }
                                .addOnFailureListener {

                                }.addOnCompleteListener {

                                }
                        }
                    ) {
                        Text(
                            text = "Try now"
                        )
                    }

                }
            }
        })

}


