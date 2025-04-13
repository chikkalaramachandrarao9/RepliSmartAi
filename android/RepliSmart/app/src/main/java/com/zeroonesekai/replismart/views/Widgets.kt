package com.zeroonesekai.replismart.views

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeroonesekai.replismart.R
import com.zeroonesekai.replismart.network.response.Review
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


/**
 * Author: Ramachandrarao Chikkala
 * Created on: 11/04/25
 */


@Composable
fun TypingText(
    text: String,
    typingSpeedMillis: Long = 20L,
    modifier: Modifier = Modifier
) {
    var currentText by remember { mutableStateOf("") }
    var showCursor by remember { mutableStateOf(true) }


    LaunchedEffect(text) {
        currentText = ""
        for (char in text) {
            currentText += char
            delay(typingSpeedMillis)
        }
        showCursor = false
    }

    val textAlpha by animateFloatAsState(
        targetValue = if (currentText.isNotEmpty()) 1f else 0f,
        animationSpec = tween(400),
        label = "text-fade-in"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.7f),
                        Color.White.copy(alpha = 0.5f)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                append(currentText)
                if (showCursor) {
                    withStyle(
                        style = SpanStyle(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF9C7CFF), Color(0xFF00C3FF)),
                                tileMode = TileMode.Mirror
                            )
                        )
                    ) {
                        append("▍")
                    }
                }
            },
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF9C7CFF), Color(0xFF00C3FF)),
                    tileMode = TileMode.Mirror
                )
            ),
            modifier = Modifier.alpha(textAlpha)
        )
    }
}


@Composable
fun RatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val starSize = 32.dp
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF9C7CFF),
            Color(0xFF00C3FF)
        )
    )
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        (1..5).forEach { star ->
            val isSelected = star <= rating
            val animatedScale by animateFloatAsState(
                targetValue = if (isSelected) 1.2f else 1f,
                animationSpec = tween(300),
                label = "StarScaleAnimation"
            )

            Canvas(
                modifier = Modifier
                    .size(starSize)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    }
                    .clickable(
                        onClick = { onRatingChanged(star) },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            ) {
                val path = Path().apply {
                    val midX = size.width / 2
                    val midY = size.height / 2
                    val radius = size.minDimension / 2
                    val innerRadius = radius * 0.5f
                    val angle = Math.toRadians(-90.0)

                    for (i in 0 until 10) {
                        val r = if (i % 2 == 0) radius else innerRadius
                        val theta = angle + Math.toRadians(i * 36.0)

                        val x = midX + (cos(theta) * r).toFloat()
                        val y = midY + (sin(theta) * r).toFloat()

                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }

                    close()
                }

                drawPath(
                    path = path,
                    brush = if (isSelected) gradientBrush else SolidColor(unselectedColor)
                )
            }
        }
    }
}


@Composable
fun GeminiAILoader(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "gemini_ai_loader")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation_anim"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale_anim"
    )

    val dotCount = 3
    val radius = 30.dp
    val dotSize = 16.dp

    val brush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF9C7CFF),
            Color(0xFF00C3FF)
        )
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(radius * 2)) {
            rotate(rotation) {
                for (i in 0 until dotCount) {
                    val angle = 2 * PI * i / dotCount
                    val x = center.x + radius.toPx() * cos(angle).toFloat()
                    val y = center.y + radius.toPx() * sin(angle).toFloat()

                    drawCircle(
                        brush = brush,
                        radius = (dotSize.toPx() / 2) * scale,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}


@Composable
fun FilePicker(
    modifier: Modifier = Modifier,
    getFile: ManagedActivityResultLauncher<String, Uri?>,
    selectedFileName: String,
    selectedFileUri: Uri?,
    context: Context,
    afterPicked: (Uri?) -> Unit
) {
    val hasFile = selectedFileUri != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Upload FAQ",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )


        ElevatedWhiteButton(
            text = "Select Xlsx File", modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            getFile.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        }


        if (hasFile) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .shadow(1.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF8F9FB), // Light elegant background
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)) // Subtle light border
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_excel_dark_slate_grey),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = selectedFileName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Button(
                        onClick = { afterPicked(selectedFileUri) },
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Upload", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

        }
    }
}


@Composable
fun AIAssistantBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF0EFFF),
                        Color(0xFFEAF8FF)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            repeat(50) {
                val x = Random.nextFloat() * size.width
                val y = Random.nextFloat() * size.height
                drawCircle(
                    color = Color.White,
                    radius = Random.nextFloat() * 2f,
                    center = Offset(x, y)
                )
            }
        }
    }
}


@Composable
fun ElevatedWhiteButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(50),
                ambientColor = Color(0xFF9C7CFF).copy(alpha = 0.3f), // Glow Color
                spotColor = Color(0xFF9C7CFF).copy(alpha = 0.3f)
            )
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(50),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Composable
fun AiAssistantInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    val isHintVisible = value.isEmpty()

    Column {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                color = Color(0xFF8C8C8C),
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(50),
                    ambientColor = Color(0xFF9C7CFF).copy(alpha = 0.15f),
                    spotColor = Color(0xFF9C7CFF).copy(alpha = 0.15f)
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isHintVisible,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { -it / 2 }
                ) {
                    Text(
                        text = label,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                    cursorBrush = SolidColor(Color.Black)
                )
            }
        }
    }
}

@Composable
fun AiChatTextArea(
    label: String,
    value: String,
    maxLines: Int = 6,
    placeholder: String = "Type your review...",
    onValueChange: (String) -> Unit,
) {
    val isHintVisible = value.isEmpty()


    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                color = Color(0xFF8C8C8C),  // Subtle Gray
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = Color(0xFF9C7CFF).copy(alpha = 0.1f),
                    spotColor = Color(0xFF9C7CFF).copy(alpha = 0.1f)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = isHintVisible,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 }
            ) {
                Text(
                    text = placeholder,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxSize(),
                maxLines = maxLines,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                cursorBrush = SolidColor(Color.Black)
            )
        }
    }
}


@Composable
fun AiDrawerContent(
    selectedScreen: String,
    onScreenSelected: (String) -> Unit,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFF9C7CFF), Color(0xFF00C3FF))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {

        Text(
            text = "RepliSmart",
            style = MaterialTheme.typography.headlineLarge.copy(
                brush = gradientBrush,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        DrawerButton(
            label = "Home",
            isSelected = selectedScreen == "Home",
            onClick = {
                onScreenSelected("Home")
                scope.launch { drawerState.close() }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        DrawerButton(
            label = "FAQ Update",
            isSelected = selectedScreen == "FAQ Update",
            onClick = {
                onScreenSelected("FAQ Update")
                scope.launch { drawerState.close() }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        DrawerButton(
            label = "Try It",
            isSelected = selectedScreen == "Try It",
            onClick = {
                onScreenSelected("Try It")
                scope.launch { drawerState.close() }
            }
        )

    }
}


@Composable
fun DrawerButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
    val textColor =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
//    val glowEffect = if (isSelected) Modifier.shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)) else Modifier

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        )
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewScreen(
    reviews: MutableList<Review?>,
    selectedType: String,
    isLoading: MutableState<Boolean>,
    onTypeSelected: (String) -> Unit,
    onLoadMore: () -> Unit,
    onItemClick: (Review) -> Unit,
    modifier: Modifier = Modifier
) {
    val types = listOf(
        "All",
        "Appreciation",
        "Feature Request",
        "General Feedback",
        "Bug Report",
        "Complaint",
        "Other"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            types.forEach { type ->
                FilterChip(
                    selected = type == selectedType,
                    onClick = { onTypeSelected(type) },
                    label = {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = if (type == selectedType) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF9C7CFF),
                        selectedLabelColor = Color.White,
                        containerColor = Color.White.copy(alpha = 0.05f),
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    leadingIcon = if (type == selectedType) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    } else null,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(reviews.size) { it ->
                reviews[it]?.let { it1 ->
                    ReviewCard(it1, Modifier.clickable {
                        onItemClick(it1)
                    })
                }
            }

            item {
                if (isLoading.value) {
                    GeminiAILoader(modifier.fillMaxWidth())
                } else {
                    ElevatedWhiteButton(text = "Load More") {
                        onLoadMore()
                    }
                }
            }
        }
    }
}


@Composable
fun ReviewCard(
    review: Review,
    modifier: Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 0.dp,
        color = Color.White.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = review.authorName?.firstOrNull()?.uppercase() ?: "",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Black
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        review.starRating?.let {
                            repeat(it) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = review?.date ?: "",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = review.authorName ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = review.comment ?: "",
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
fun AiReviewDetailCard(
    review: Review,
    replyText: String,
    isLoading: MutableState<Boolean>,
    onReplyTextChange: (String) -> Unit,
    onGenerateReply: () -> Unit,
    onSendReply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF9F9FB), // Light soft background
        border = BorderStroke(
            1.dp, Brush.horizontalGradient(
                listOf(Color(0xFF9C7CFF), Color(0xFF00C3FF))
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Reviewer
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = review.authorName?.firstOrNull()?.uppercase() ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = review.authorName ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = review.date ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(review.starRating ?: 5) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = review.comment ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = review.deviceMetadata ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading.value) {
                GeminiAILoader(Modifier.fillMaxWidth())
            } else
                AiReplyInputField(
                    replyText = replyText,
                    onReplyTextChange = onReplyTextChange,
                    onGenerateReply = onGenerateReply,
                    onSendReply = onSendReply
                )
        }
    }
}


@Composable
fun AiReplyInputField(
    replyText: String,
    onReplyTextChange: (String) -> Unit,
    onGenerateReply: () -> Unit,
    onSendReply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .border(
                width = 1.dp,
                color = Color(0xFFE2E8F0), // subtle border
                shape = RoundedCornerShape(30.dp)
            )
            .background(Color.White.copy(alpha = 0.5f)), // clear light bg,
        tonalElevation = 8.dp,
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onGenerateReply) {
                Icon(
                    painter = painterResource(id = R.drawable.ai_gen),
                    contentDescription = "AI Generate",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                contentAlignment = if (replyText.isEmpty()) Alignment.Center else Alignment.CenterStart
            ) {
                BasicTextField(
                    value = replyText,
                    onValueChange = onReplyTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    decorationBox = { innerTextField ->
                        if (replyText.isEmpty()) {
                            Text(
                                text = "Reply...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(onClick = onSendReply) {
                Icon(
                    painter = painterResource(id = R.drawable.communication),
                    contentDescription = "Send Reply",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}





















