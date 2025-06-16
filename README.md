# MyLiteRTExperiment

A modern Android app built with **Kotlin 2.0.x**, **Jetpack Compose**, and the **MVVM** architecture, leveraging multiple on-device ML models using the new **LiteRT Next (CompiledModel) APIs**. The app overlays results from all models on a live **CameraX** preview, providing a fast, future-proof, and fully on-device AI experience.

## Features

- **Live CameraX Preview** with real-time ML overlays
- **Image Classification** (MobileNetV3, ImageNet labels)
- **Style Transfer**
- **OCR (Optical Character Recognition)**
- **Speech-to-Text**
- **Pure Compose UI** with Material 3 theming
- **MVVM architecture** with repository and model executor layers
- **All models use LiteRT Next (CompiledModel)** — no classic TFLite code

## Requirements

- **Android Studio Giraffe or newer**
- **Kotlin 2.0.x**
- **Jetpack Compose BOM alpha (2025.06.00 or newer)**
- **Gradle 8.14.2**
- **Android device with CameraX and ML support**

## Setup Instructions

1. **Clone the repository:**
   ```sh
   git clone <your-repo-url>
   cd MyLiteRTExperiment
   ```
2. **Open in Android Studio.**
3. **Sync Gradle and build the project.**
4. **Run on a real device** (emulator support may be limited for CameraX and on-device ML).

### Model and Asset Files

- Place all model files (`.tflite`) in `app/src/main/assets/`.
- For image classification, use the **standard MobileNetV3 float32 model** with 1001 output classes (see [TensorFlow Lite hosted models](https://www.tensorflow.org/lite/guide/hosted_models)).
- Place the `ImageNetLabels.txt` file (with 1001 labels) in the same assets directory.

### Model Compatibility

- **Always use models compatible with LiteRT Next.**
- **Check model input/output shapes and types** using [Netron](https://netron.app/):
  - Input: `float32[1,224,224,3]` (for MobileNetV3)
  - Output: `float32[1,1001]`
- **Do not use custom or non-standard models** (e.g., with 1000 outputs) unless you update all code and labels accordingly. Incompatible models may cause native crashes.

### Troubleshooting

- **Native crash during inference?**
  - Check that your model file is not corrupt and matches the expected input/output shapes and types.
  - Use only official, standard models for best compatibility.
  - Ensure only one inference runs at a time per model instance.
- **Camera not working?**
  - Make sure camera permissions are granted.
  - Test on a real device.
- **Gradle or Compose errors?**
  - Ensure you are using the correct versions as listed above.

## Contributing

Contributions are welcome! Please open issues or pull requests for bug fixes, improvements, or new features. For major changes, please discuss them in an issue first.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details. 