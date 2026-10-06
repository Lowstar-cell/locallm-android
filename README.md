# LocalLM – Android chat for your own LM Studio server

An Android app (Kotlin, Jetpack Compose) for chatting with the language models
running on your own PC in [LM Studio](https://lmstudio.ai). It works at home
over Wi-Fi and on the road through a [Tailscale](https://tailscale.com)
connection to the PC — no cloud service involved.

Through LM Studio's MCP integrations the model can use tools on the PC: web
search, image generation, file system, terminal, Blender and more. Generated
images are shown directly in the chat.

## Features

- Model list loaded from the server, switchable per chat
- MCP tools toggled in the settings: image generation, web search, file
  system, terminal, Blender, date/time, plus any custom MCP server by name
- Image intent detection: the image tool is only offered when the message asks
  for a picture ("draw", "create an image", …)
- Images found in tool results (data URLs or image links) are rendered inline
- Voice input (Android speech recognition) and spoken answers (text-to-speech)
- System prompt and stored prompts
- Optional API token (Bearer) for LM Studio's authentication; the app checks
  whether the server requires it
- Chats can be saved

## How it connects

```
Phone ──Wi-Fi or Tailscale──▶ PC: LM Studio server (port 1234)
                                   ├─ /v1/models
                                   ├─ /v1/chat/completions      plain chat
                                   └─ /api/v1/chat              chat with MCP integrations
```

In the app settings enter the PC's address (LAN IP at home, Tailscale IP or
MagicDNS name on the road) and the port. In LM Studio enable *Serve on local
network*.

## Build

Open the project in Android Studio, or:

```bash
./gradlew assembleDebug
```

minSdk 24, targetSdk 36. Cleartext HTTP is allowed on purpose
(`network_security_config.xml`), since LM Studio serves plain HTTP inside the
LAN/tailnet.

## Tech

Kotlin · Jetpack Compose (Material 3) · ViewModel + StateFlow · DataStore ·
Retrofit / OkHttp / Gson · Android SpeechRecognizer and TextToSpeech
