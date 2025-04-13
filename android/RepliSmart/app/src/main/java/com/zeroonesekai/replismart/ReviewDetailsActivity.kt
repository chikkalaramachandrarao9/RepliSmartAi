package com.zeroonesekai.replismart

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.zeroonesekai.replismart.network.request.ReviewRequest
import com.zeroonesekai.replismart.network.response.Review
import com.zeroonesekai.replismart.ui.theme.RepliSmartTheme
import com.zeroonesekai.replismart.viewmodel.ApiResource
import com.zeroonesekai.replismart.viewmodel.ReviewViewmodel
import com.zeroonesekai.replismart.views.AIAssistantBackground
import com.zeroonesekai.replismart.views.AiReviewDetailCard
import kotlinx.coroutines.launch


/**
 * Author: Ramachandrarao Chikkala
 * Created on: 12/04/25
 */
class ReviewDetailsActivity : ComponentActivity() {

    private var review1: Review? = null

    private val viewModel by lazy {
        ViewModelProvider(this)[ReviewViewmodel::class.java]
    }


    companion object {
        const val TAG = "ReviewDetailsActivity"
        const val EXTRA_REVIEW = "EXTRA_REVIEW"
        fun start(context: Context, review: Review) {
            val intent = Intent(context, ReviewDetailsActivity::class.java)
            intent.putExtra(EXTRA_REVIEW,review )
            context.startActivity(intent)
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        review1 = intent.getSerializableExtra(EXTRA_REVIEW) as Review?
        setContent {
            val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
            RepliSmartTheme {

                val scope = rememberCoroutineScope()
                val reply = remember { mutableStateOf("") }
                val reviewResponse by viewModel.reviewResponse.collectAsState()
                val isLoading = remember { mutableStateOf(false) }
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(reviewResponse.status) {
                    when (reviewResponse.status) {
                        ApiResource.Status.LOADING -> {
                            isLoading.value = true
                            reply.value = ""
                        }

                        ApiResource.Status.SUCCESS -> {
                            isLoading.value = false
                            reviewResponse.data?.let { response ->
                                reply.value = response.response.reply
                            }
                        }

                        ApiResource.Status.ERROR -> {
                            isLoading.value = false
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar(
                                    reviewResponse.message ?: "Error occurred",
                                    actionLabel = "OK"
                                )
                            }
                        }

                        ApiResource.Status.IDLE -> {
                            isLoading.value = false
                            reply.value = ""
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(horizontal = 16.dp, vertical = 24.dp)
                                .padding(top = 50.dp, bottom = 15.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            IconButton(
                                onClick = {
                                    finish()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Menu",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = "RepliSmart",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF000000),
                                            Color(0xFF9C7CFF),
                                            Color(0xFFB295FF)
                                        )
                                    ),
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 16.dp)
                            )
                        }
                    },
                    containerColor = Color.White
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()

                    ) {
                        AIAssistantBackground()
                    }
                    review1?.let { review ->
                        AiReviewDetailCard(
                            review,
                            replyText = reply.value,
                            isLoading,
                            onReplyTextChange = {
                                reply.value = it
                            },
                            onGenerateReply = {
                                viewModel.postReview(
                                    ReviewRequest(
                                        review.starRating ?: 5,
                                        review.comment ?: "",
                                        review.authorName ?: ""
                                    )
                                )
                            },
                            onSendReply = {
                                scope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    snackbarHostState.showSnackbar("Reply published!", actionLabel = "OK")
                                    finish()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(innerPadding)
                        )
                    }
                }

            }
        }
    }


}