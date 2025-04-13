package com.zeroonesekai.replismart

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.zeroonesekai.replismart.network.request.ReviewRequest
import com.zeroonesekai.replismart.network.response.Review
import com.zeroonesekai.replismart.ui.theme.RepliSmartTheme
import com.zeroonesekai.replismart.viewmodel.ApiResource
import com.zeroonesekai.replismart.viewmodel.ReviewViewmodel
import com.zeroonesekai.replismart.views.AIAssistantBackground
import com.zeroonesekai.replismart.views.AiAssistantInputField
import com.zeroonesekai.replismart.views.AiChatTextArea
import com.zeroonesekai.replismart.views.AiDrawerContent
import com.zeroonesekai.replismart.views.FilePicker
import com.zeroonesekai.replismart.views.GeminiAILoader
import com.zeroonesekai.replismart.views.ElevatedWhiteButton
import com.zeroonesekai.replismart.views.RatingBar
import com.zeroonesekai.replismart.views.ReviewScreen
import com.zeroonesekai.replismart.views.TypingText
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

class MainActivity : ComponentActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this)[ReviewViewmodel::class.java]
    }


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.getReviews(1,"All")
        setContent {
            val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
            RepliSmartTheme {

                val reviewsList = remember { mutableStateListOf<Review?>(null) }
                val resetValues = remember { mutableStateOf(false) }

                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                var selectedScreen by remember {
                    mutableStateOf("Home", policy = object :
                        SnapshotMutationPolicy<String> {
                        override fun equivalent(a: String, b: String): Boolean {
                            if (a != b) {
                                viewModel.resetResponses()
                            }
                            return a == b
                        }
                    })
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            AiDrawerContent(
                                selectedScreen,
                                onScreenSelected = {
                                    selectedScreen = it
                                    if (selectedScreen == "Home") {
                                        reviewsList.clear()
                                        viewModel.getReviews(1,"All")
                                    }
                                },
                                drawerState = drawerState,
                                scope = scope
                            )
                        }
                    },
                ) {

                    Scaffold(
                        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                                        scope.launch {
                                            drawerState.open()
                                        }
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.menu),
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

                                if(selectedScreen == "Home" || selectedScreen == "Try It") {

                                    IconButton(
                                        onClick = {
                                            if(selectedScreen == "Home") {
                                                reviewsList.clear()
                                                viewModel.getReviews(1,"All")
                                            } else {
                                                viewModel.resetResponses()
                                                resetValues.value = true
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Menu",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
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
                        when (selectedScreen) {
                            "Home" -> ReviewListingScreen(
                                reviewsList,
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                            )

                            "FAQ Update" -> FileUploadScreen(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                            )

                            "Try It" -> LeaveAReviewScreen(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize(),
                                resetValues = resetValues
                            )


                        }
                    }
                }
            }
        }
    }


    @Composable
    fun ReviewListingScreen(
        reviewsList: SnapshotStateList<Review?>,
        modifier: Modifier = Modifier,
    ) {

        val pageNo = remember { mutableIntStateOf(1) }
        val reviewsListResponse by viewModel.reviewsListResponse.collectAsState()
        val isLoading = remember { mutableStateOf(false) }
        val selectedCategory = remember { mutableStateOf("All") }

        LaunchedEffect(reviewsListResponse.status) {
            when (reviewsListResponse.status) {
                ApiResource.Status.LOADING -> {
                    isLoading.value = true
                }

                ApiResource.Status.SUCCESS -> {
                    isLoading.value = false
                    reviewsListResponse.data?.let { reviewsList.addAll(it) }
                }

                ApiResource.Status.ERROR -> {
                    isLoading.value = false
                    reviewsListResponse.message?.let { message ->
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }

                ApiResource.Status.IDLE -> {
                    isLoading.value = false
                }
            }
        }


        ReviewScreen(
            reviewsList,
            selectedType = selectedCategory.value,
            isLoading,
            onTypeSelected = {
                selectedCategory.value = it
                pageNo.intValue = 1
                reviewsList.clear()
                viewModel.getReviews(pageNo.intValue,it)
            },
            onLoadMore = {
                viewModel.getReviews(++pageNo.intValue,selectedCategory.value)
            },
            onItemClick = {
                ReviewDetailsActivity.start(this, it)
            },
            modifier = modifier
        )


    }


    @Composable
    fun LeaveAReviewScreen(
        modifier: Modifier = Modifier,
        resetValues: MutableState<Boolean>
    ) {
        val reviewText = remember { mutableStateOf("") }
        val rating = remember { mutableIntStateOf(0) }
        val responseMessage = remember { mutableStateOf("") }
        val userName = remember { mutableStateOf("") }
        val reviewResponse by viewModel.reviewResponse.collectAsState()
        val isLoading = remember { mutableStateOf(false) }
        val category = remember { mutableStateOf("") }

        LaunchedEffect(reviewResponse.status) {
            when (reviewResponse.status) {
                ApiResource.Status.LOADING -> {
                    isLoading.value = true
                    responseMessage.value = ""
                    category.value = ""
                }

                ApiResource.Status.SUCCESS -> {
                    isLoading.value = false
                    reviewResponse.data?.let { response ->
                        responseMessage.value = response.response.reply
                        category.value = response.response.category
                    }
                }

                ApiResource.Status.ERROR -> {
                    isLoading.value = false
                    Toast.makeText(
                        this@MainActivity,
                        reviewResponse.message ?: "Error occurred",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                ApiResource.Status.IDLE -> {
                    isLoading.value = false
                    responseMessage.value = ""
                    category.value = ""
                    rating.intValue = 0
                    userName.value = ""
                    reviewText.value = ""
                }
            }
        }

        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(vertical = 10.dp, horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            AiAssistantInputField(
                label = "User Name",
                value = userName.value,
                onValueChange = { userName.value = it }
            )

            AiChatTextArea(
                label = "Review",
                value = reviewText.value,
                onValueChange = { reviewText.value = it },
                placeholder = "Type your review..."
            )




            RatingBar(
                rating = rating.value,
                onRatingChanged = { rating.value = it }
            )

            Spacer(Modifier.height(20.dp))
            ElevatedWhiteButton(
                text = "Generate", modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (userName.value.isNotBlank() && reviewText.value.isNotBlank()) {
                    lifecycleScope.launch {
                        viewModel.postReview(
                            ReviewRequest(
                                rating.intValue,
                                reviewText.value,
                                userName.value
                            )
                        )
                    }
                }
            }

            AnimatedVisibility(visible = responseMessage.value.isNotBlank()) {
                Column {
                    Text(
                        text = "Category: ${category.value}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TypingText(
                        text = responseMessage.value,
                        typingSpeedMillis = 30L,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

            }

            AnimatedVisibility(visible = isLoading.value) {
                GeminiAILoader(modifier.size(20.dp))
            }

            if(resetValues.value) {
                reviewText.value = ""
                rating.intValue = 0
                userName.value = ""
                responseMessage.value = ""
                category.value = ""
                resetValues.value = false
            }
        }
    }


    @Composable
    fun FileUploadScreen(modifier: Modifier) {
        var selectedFileName by remember { mutableStateOf("") }
        var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
        val context = LocalContext.current
        val isLoading = remember { mutableStateOf(false) }

        val faqUpdateResponse by viewModel.faqUpdateResponse.collectAsState()

        LaunchedEffect(faqUpdateResponse.status) {
            when (faqUpdateResponse.status) {
                ApiResource.Status.LOADING -> {
                    isLoading.value = true
                }

                ApiResource.Status.SUCCESS -> {
                    isLoading.value = false
                    Toast.makeText(
                        context,
                        faqUpdateResponse.data?.message ?: "Faq uploaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                ApiResource.Status.ERROR -> {
                    isLoading.value = false
                    faqUpdateResponse.message?.let { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }

                ApiResource.Status.IDLE -> {
                    isLoading.value = false
                }
            }
        }


        val getFile =
            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    // Check if the file is an xlsx file
                    if (isExcelFile(getMimeType(context, it))) {
                        selectedFileUri = it
                        selectedFileName = it.toString()
                    } else {
                        selectedFileName = "Invalid file type. Please select an .xlsx file."
                    }
                }
            }

        if (isLoading.value) {
            GeminiAILoader(modifier.fillMaxSize())
        } else {
            FilePicker(modifier, getFile, selectedFileName, selectedFileUri, context) {
                selectedFileUri?.let { uri ->
                    val file = context.contentResolver.openInputStream(uri)
                    val requestBody = file?.let {
                        RequestBody.create(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".toMediaTypeOrNull(),
                            it.readBytes()
                        )
                    }
                    val fileName = selectedFileName.takeIf { it.endsWith(".xlsx") }
                        ?: "$selectedFileName.xlsx"

                    val body = requestBody?.let {
                        MultipartBody.Part.createFormData("file", fileName, it)
                    }
                    if (body != null) {
                        viewModel.updateFaq(body)
                    }
                }
            }
        }
    }


    private fun getMimeType(context: Context, uri: Uri): String? {
        val mimeType = context.contentResolver.getType(uri)
        return mimeType
    }

    private fun isExcelFile(mimeType: String?): Boolean {
        return mimeType == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    }


}




