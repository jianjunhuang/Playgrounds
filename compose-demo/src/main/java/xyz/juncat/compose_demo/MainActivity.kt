package xyz.juncat.compose_demo

import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.juncat.compose_demo.ui.theme.PlaygroundTheme

/**
 * code from https://developer.android.com/jetpack/compose/tutorial
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
//            PlaygroundTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(color = MaterialTheme.colors.background) {
//                    Column() {
//                        Conversation(
//                            messages = listOf(
//                                "11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111",
//                                "2",
//                                "3",
//                                "4"
//                            )
//                        )
//                        HelloContent(name = "content", onNameChange = {
//                        })
//                    }
//                }
//            }
//            compositionLocalTest()
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

data class Info(val name: String, val code: Int)

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

        val info = rememberSaveable(stateSaver = run {
            mapSaver<Info>(
                save = { mapOf("nameKey" to it.name, "codeKey" to it.code) },
                restore = { Info(it["nameKey"] as String, it["codeKey"] as Int) })
        }) {
            mutableStateOf(Info("info", 0))
        }

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

@Composable
fun HelloScreen() {
    var name by rememberSaveable { mutableStateOf("") }

    HelloContent(name = name, onNameChange = { name = it })
}


@Composable
private fun compositionLocalTest() {
    MaterialTheme {
        Column {
            Text("Normal Text")
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text("ContentAlpha = medium")
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.high,
                    LocalContentColor provides Color.Green
                ) {
//                    LocalContext.current.resources.getQuantityString()
                    Text("ContentAlpha = high, ContentColor = Green")
                }
            }
        }
    }
}

@Composable
fun HelloContent(name: String, onNameChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Hello, $name",
            modifier = Modifier.padding(bottom = 8.dp),
            style = MaterialTheme.typography.h5
        )
        var textValue by remember {
            mutableStateOf("")
        }
        OutlinedTextField(
            value = textValue,
            onValueChange = {
                onNameChange.invoke(it)
                textValue = it
            },
            label = { Text("Name") }
        )
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