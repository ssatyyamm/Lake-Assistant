# 🌊 Lake: Your Personal AI Assistant  


[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://app.devin.ai/wiki/ssatyyamm/Lake-Assistant)
---

# Demos:

#### Lake Interface
 [![Watch the video](https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ_led94S5l3sCb9L8uCNDpX_mWGpqF4TbTg51H8Ac_KQ&s=10)](https://youtube.com/shorts/I-jxjB9F9Ls?si=roLHFTkt3XK0rB5L)



**Lake** is a proactive, on-device AI agent for Android that autonomously understands natural language commands and operates your phone's UI to achieve them. Inspired by the need to make modern technology more accessible, Lake acts as your personal operator, capable of handling complex, multi-step tasks across different applications.

[![Project Status: WIP](https://img.shields.io/badge/project%20status-wip-yellow.svg)](https://wip.vost.pt/)
[![License: Personal Use](https://img.shields.io/badge/License-Personal%20Use%20Only-red.svg)](./LICENSE)
[![Kotlin Version](https://img.shields.io/badge/Kotlin-1.9.22-7F52FF.svg?logo=kotlin)](https://kotlinlang.org)

## Core Capabilities

* 🧠 **Intelligent UI Automation:** Lake sees the screen, understands the context of UI elements, and performs actions like tapping, swiping, and typing to navigate apps and complete tasks.
* 📢 **High Qaulity voice:** Lake have high quality voice by GCS's Chirp  
* 💾 **Persistent & Personalized local Memory:** ⚠️ **Temporarily Disabled** - Lake memory is turned off as of yet. Memory functionality will be restored in a future update.

## Architecture Overview

Lake is built on a sophisticated multi-agent system written entirely in Kotlin. This architecture separates responsibilities, allowing for more complex and reliable reasoning.

* **Eyes & Hands (The Actuator):** The **Android Accessibility Service** serves as the agent's physical connection to the device, providing the low-level ability to read the screen element hierarchy and programmatically perform touch gestures.
* **The Brain (The LLM):** All high-level reasoning, planning, and analysis are powered by **LLM** models. This is where decisions are made.
* **The Agent:**
    * **Operator:** This is executor with Notepad.


## 🚀 Getting Started

### Prerequisites
* Android Studio (latest version recommended)
* An Android device or emulator with API level 26+
* Some gguf and onnx model, sample ENV
```python
# you can use any servers that can accept requests, i will improve the developer experience in the future by making openapi compatible
```
`payload`
```
{
  "modelName": "model-name",
  "messages": [
    {
      "role": "user",
      "parts": [
        {
          "text": "Hello, what can you do?"
        }
      ]
    },
    {
      "role": "model",
      "parts": [
        {
          "text": "I can help you with a variety of tasks. What do you need assistance with today?"
        }
      ]
    }
  ]
}
```
or
```
//you can also add gguf model MiniPLM-Qwen-200M-Q8_0.gguf in assist and Piper TTS en_GB-cori-medium.onnx



```




### Installation

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/ssatyyamm/Lake-Assistant.git](https://github.com/ssatyyamm/Lake-Assistant.git)
    cd Lake-Assistant
    ```

2.  **Build & Run:**
    * Open the project in Android Studio.
    * Let Gradle sync all the dependencies.
    * Run the app on your selected device or emulator.

3.  **Enable Accessibility Service:**
    * On the first run, the app will prompt you to grant Accessibility permission.
    * Click "Grant Access" and enable the "Lake" service in your phone's settings. This is required for the agent to see and control the screen.

## 🗺️ What's Next for Lake (Roadmap)

Lake is currently a powerful proof-of-concept, and the roadmap is focused on making it a truly indispensable assistant.

* [ ] **NOT UPDATED:** List not updated

## 🤝 Contributing

Contributions are welcome! If you have ideas for new features or improvements, feel free to open an issue or submit a pull request.

## 📜 License

This project is licensed under a Personal Use License - see the [LICENSE](LICENSE) file for details.

**Personal & Educational Use:** Free to use, modify, and distribute for personal, educational, and non-commercial purposes.

**Commercial Use:** Requires a separate commercial license. Please contact Lake AI for commercial licensing terms.


Write you api key in in local.properties, more keys you use, better is the speed 😉

# View logs in real-time
adb logcat | Our Offline

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=ssatyyamm/lake&type=Timeline)](https://www.star-history.com/#ssatyyamm/lake&Timeline)
