package com.bless.jarvis.ui
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bless.jarvis.viewmodel.ChatViewModel
private val Bg=Color(0xFF080A0F);private val Panel=Color(0xFF11141B);private val Accent=Color(0xFF8AB4FF)
@Composable fun JarvisApp(vm:ChatViewModel){MaterialTheme(colorScheme=darkColorScheme(background=Bg,surface=Panel,primary=Accent)){var tab by remember{mutableIntStateOf(0)};Scaffold(containerColor=Bg,bottomBar={NavigationBar(containerColor=Panel){listOf("JARVIS" to Icons.Default.AutoAwesome,"Activity" to Icons.Default.Timeline,"Body" to Icons.Default.PhoneAndroid).forEachIndexed{i,p->NavigationBarItem(tab==i,{tab=i},{Icon(p.second,null)},label={Text(p.first)})}}}){p->Box(Modifier.padding(p)){when(tab){0->Home(vm);1->Activity(vm);2->Body()}}}}}
@Composable private fun Home(vm:ChatViewModel){val msgs by vm.messages.collectAsStateWithLifecycle();val state by vm.state.collectAsStateWithLifecycle();val online by vm.online.collectAsStateWithLifecycle();val activity by vm.activity.collectAsStateWithLifecycle();var input by remember{mutableStateOf("")};Column(Modifier.fillMaxSize().padding(20.dp)){Row(verticalAlignment=Alignment.CenterVertically){Column{Text("JARVIS",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text("Cognitive Android",color=Color.Gray,style=MaterialTheme.typography.bodySmall)};Spacer(Modifier.weight(1f));Text(if(online)"ONLINE" else "OFFLINE",color=Accent,style=MaterialTheme.typography.labelSmall)};Spacer(Modifier.height(22.dp));Box(Modifier.fillMaxWidth().height(185.dp),Alignment.Center){Box(Modifier.size(142.dp).clip(CircleShape).background(Accent.copy(.08f)),Alignment.Center){Box(Modifier.size(88.dp).clip(CircleShape).background(Accent.copy(.16f)),Alignment.Center){Icon(Icons.Default.AutoAwesome,null,tint=Accent,modifier=Modifier.size(38.dp))}}};Text(state,Modifier.align(Alignment.CenterHorizontally),fontWeight=FontWeight.Bold);Text(activity,Modifier.align(Alignment.CenterHorizontally),color=Color.Gray);Spacer(Modifier.height(14.dp));LazyColumn(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(9.dp)){items(msgs){m->Surface(color=if(m.fromJarvis)Panel else Accent.copy(.13f),shape=RoundedCornerShape(17.dp)){Text(m.text,Modifier.padding(13.dp))}}};Row(verticalAlignment=Alignment.CenterVertically){OutlinedTextField(input,{input=it},Modifier.weight(1f),singleLine=true,placeholder={Text("Talk to JARVIS...")},shape=RoundedCornerShape(24.dp));IconButton({vm.send(input);input=""}){Icon(Icons.Default.ArrowUpward,null,tint=Accent)}}}}
@Composable private fun Activity(vm:ChatViewModel){val msgs by vm.messages.collectAsStateWithLifecycle();Column(Modifier.fillMaxSize().padding(20.dp)){Text("Activity",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("A window into JARVIS thinking and action",color=Color.Gray);Spacer(Modifier.height(18.dp));LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)){items(msgs.reversed()){m->Surface(Panel,RoundedCornerShape(16.dp),Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text(if(m.fromJarvis)"JARVIS" else "YOU",color=Accent,style=MaterialTheme.typography.labelSmall);Text(m.text)}}}}}}
@Composable private fun Body(){Column(Modifier.fillMaxSize().padding(20.dp)){Text("Android Body",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("Capabilities exposed to the brain",color=Color.Gray);Spacer(Modifier.height(18.dp));listOf("open_app" to "Launch applications","launch_url" to "Open links","device_info" to "Device information","get_battery" to "Battery state","get_volume" to "Read volume","set_volume" to "Control volume","open_settings" to "Android settings").forEach{(n,d)->Surface(Panel,RoundedCornerShape(15.dp),Modifier.fillMaxWidth().padding(bottom=9.dp)){Row(Modifier.padding(15.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Extension,null,tint=Accent);Spacer(Modifier.width(13.dp));Column{Text(n,fontWeight=FontWeight.SemiBold);Text(d,color=Color.Gray,style=MaterialTheme.typography.bodySmall)}}}}}}
