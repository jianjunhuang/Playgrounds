package xyz.juncat.compose_demo

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.juncat.compose_demo.ui.theme.PlaygroundTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Conversation(
                        messages = listOf(
                            "11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111",
                            "2",
                            "3",
                            "4"
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun Conversation(messages: List<String>) {
    LazyColumn {
        items(messages) { message ->
            Greeting(message)
        }
    }
}

@Composable
fun Greeting(name: String) {
    Row(modifier = Modifier.padding(16.dp)) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "android",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Black, CircleShape)
        )
        Spacer(modifier = Modifier.size(30.dp))

        //store state by remember
        var isExpanded by remember {
            //track change
            mutableStateOf(false)
        }

        val surfaceColor: Color by animateColorAsState(
            if (isExpanded) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
        )

        Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
            Text(text = "Hello !", color = Color.Gray)
            Spacer(modifier = Modifier.size(3.dp))
            Text(text = "Hi!", color = Color.Red, style = MaterialTheme.typography.h5)

            Surface(
                shape = MaterialTheme.shapes.large,
                elevation = 1.dp,
                color = surfaceColor,
                modifier = Modifier
                    .animateContentSize()
                    .padding(1.dp)
            ) {
                Text(text = name, maxLines = if (isExpanded) Int.MAX_VALUE else 1)
            }
        }
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DefaultPreview() {
    PlaygroundTheme {
        Conversation(
            messages = listOf(
                "111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111",
                "2",
                "3",
                "4"
            )
        )
    }
}