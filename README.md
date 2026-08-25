# 📱 English-AI-Learning-App

Ứng dụng học tiếng Anh thông minh hỗ trợ bởi Trí tuệ Nhân tạo (**Llama 3.1**) kết hợp nhận diện văn bản OCR và quản lý học tập cá nhân hóa.

---

## 🌟 Các tính năng chính

- 🤖 **Trợ lý Chat AI Ngôn ngữ (Llama 3.1)**: Hỏi đáp ngữ pháp, tra từ vựng, sửa lỗi câu và luyện hội thoại tiếng Anh thời gian thực.
- 📄 **Tóm tắt văn bản bằng AI (Llama 3.1)**: Cô đọng các tài liệu, bài đọc dài thành các luận điểm chính ngắn gọn, dễ hiểu.
- 📷 **Quét tài liệu & OCR**: Chụp ảnh hoặc chọn tài liệu để trích xuất văn bản tức thì.
- 📚 **Học từ vựng & Flashcards**: Lưu trữ thư mục từ vựng, ôn tập theo phương pháp lặp lại ngắt quãng.
- 🌐 **Dịch văn bản**: Hỗ trợ dịch nhanh nội dung học tập.

---

## 🛠️ Hướng dẫn Cài đặt & Setup AI (Ollama + Llama 3.1)

> **Người thực hiện**: Nguyễn Đăng Khôi  
> **Tính năng phụ trách**: 
> 1. *Use case “Chat với AI”* (Trợ lý ngôn ngữ Llama 3.1)
> 2. *Use case “Tóm tắt văn bản bằng AI”* (Cô đọng văn bản thông minh)

Hệ thống AI chạy cục bộ (Local) trên máy tính thông qua **Ollama** và được kết nối tới ứng dụng Android qua **Server Python trung gian**.

---

### 1. Cài đặt Ollama trên máy tính

#### Trên Linux / Ubuntu / WSL:
Chạy lệnh cài đặt chính thức bằng 1 dòng lệnh:
```bash
curl -fsSL https://ollama.com/install.sh | sh
```

#### Trên Windows / macOS:
- Tải bộ cài đặt trực tiếp tại trang chủ: [https://ollama.com/download](https://ollama.com/download)
- Cài đặt như phần mềm thông thường.

---

### 2. Tải và Khởi động mô hình Llama 3.1

Mở Terminal / Command Prompt trên máy tính và chạy:

```bash
ollama run llama3.1
```

> 💡 **Lưu ý về cấu hình máy tính:**
> - **Máy có RAM $\ge$ 8GB - 16GB**: Khuyên dùng `llama3.1` (kích thước ~4.7 GB).
> - **Máy có cấu hình nhẹ hơn (RAM < 8GB)**: Có thể dùng mô hình siêu nhẹ `llama3.2` (~2.0 GB, phản hồi cực nhanh):
>   ```bash
>   ollama run llama3.2
>   ```
>   *(Nếu dùng `llama3.2`, bạn chỉ cần khai báo biến trước khi bật server: `export OLLAMA_MODEL="llama3.2"`)*.

Kiểm tra Ollama đã sẵn sàng hoạt động tại cổng `11434`:
```bash
curl http://localhost:11434/api/tags
```

---

### 3. Khởi động Backend Server Python

Server Python đóng vai trò cầu nối tiếp nhận yêu cầu từ ứng dụng Android (Endpoint `/chat` và `/summarize`) và gọi Ollama xử lý.

1. Di chuyển vào thư mục `server`:
   ```bash
   cd server
   ```
2. Chạy server:
   ```bash
   python3 app.py
   ```

Server hỗ trợ chạy trực tiếp với thư viện chuẩn của Python (không bắt buộc cài thêm thư viện ngoài). Khi khởi động thành công, màn hình sẽ hiển thị:
```text
=================================================================
🚀 EnglishAI Server (Llama 3.1 + Ollama) đang khởi động...
📡 Đang lắng nghe: http://0.0.0.0:5000
🤖 Mô hình Ollama: llama3.1 (tại http://localhost:11434/api/generate)
📌 Endpoint Tóm tắt: POST http://localhost:5000/summarize
📌 Endpoint Chat AI:  POST http://localhost:5000/chat
=================================================================
```

---

### 4. Kết nối Điện thoại Android với Máy tính

Ứng dụng hỗ trợ linh hoạt cả **Cáp USB** và **Mạng Wifi LAN**:

#### 🔌 Cách 1: Kết nối qua cáp USB Debugging (Khuyên dùng - Ổn định & Nhanh nhất)
1. Cắm cáp USB nối điện thoại với máy tính, bật **USB Debugging** (Gỡ lỗi USB) trên điện thoại.
2. Trên máy tính, mở Terminal / CMD và chạy lệnh chuyển tiếp cổng:
   ```bash
   adb reverse tcp:5000 tcp:5000
   ```
3. Mở ứng dụng Android, hệ thống sẽ tự động kết nối qua `127.0.0.1:5000`.

#### 📶 Cách 2: Kết nối qua mạng Wifi LAN (Không cần dây cáp)
1. Đảm bảo điện thoại và máy tính kết nối **chung một mạng Wifi**.
2. Tìm địa chỉ IP nội bộ của máy tính:
   - **Windows**: Chạy `cmd` $\rightarrow$ gõ `ipconfig` $\rightarrow$ xem `IPv4 Address` (ví dụ: `192.168.1.50` hoặc `10.20.247.xxx`).
   - **Linux**: Chạy `hostname -I`.
   - **macOS**: Chạy `ipconfig getifaddr en0`.
3. Trong ứng dụng Android:
   - Nhấn vào biểu tượng **⚙️ (Cài đặt Server)** ở góc trên màn hình Chat AI hoặc Chi tiết văn bản.
   - Nhập IP máy tính vừa tìm được và bấm **"Lưu & Áp dụng"**.

---

## 🎯 Hướng dẫn trải nghiệm 2 Use Case AI trên App

### 1. Use case: “Chat với AI” (Trợ lý AI Ngôn ngữ)
1. Tại màn hình chính, nhấn vào card **"Trợ lý Chat AI"** (hoặc chọn tab **Chat AI** ở thanh điều hướng dưới cùng).
2. Nhập câu hỏi vào ô *"Nhập tin nhắn..."* (ví dụ: *"hiện tại đơn là thì như nào"*, *"dạy mình 5 từ vựng giao tiếp"*, *"sửa lỗi câu: I goes to school"*) hoặc nhấn vào các thẻ gợi ý nhanh.
3. Tin nhắn người dùng xuất hiện ở **bong bóng màu tím bên phải**.
4. Hiệu ứng *"AI đang suy nghĩ..."* kích hoạt trong khi Llama 3.1 xử lý.
5. Câu trả lời của AI xuất hiện ở **bong bóng bên trái**, danh sách tự động cuộn xuống dưới cùng.
6. Chạm vào bất kỳ tin nhắn nào để sao chép nhanh vào bộ nhớ tạm.

### 2. Use case: “Tóm tắt văn bản bằng AI”
1. Chọn tab **"Quét"** ở thanh điều hướng dưới cùng.
2. Nhấn **"QUÉT TÀI LIỆU MỚI"** (để trích xuất ảnh) hoặc nhấn **"Mở văn bản mẫu thử Tóm tắt AI"** / nhấn **"Xem"** trên một tệp có sẵn.
3. Tại màn hình **Chi tiết văn bản**, nhấn vào tab **"Tóm tắt AI"**.
4. Ứng dụng hiển thị hiệu ứng vòng xoay tải ngầm và gửi văn bản tới server.
5. Mô hình Llama 3.1 phân tích ngữ nghĩa và trả về các luận điểm chính dạng gạch đầu dòng rõ ràng, trực quan.

---

## 🏗️ Cấu trúc thư mục dự án

```text
English-AI-Learning-App/
├── README.md                           # Tài liệu hướng dẫn dự án
├── server/                             # Backend Server trung gian
│   ├── app.py                          # Flask & Built-in HTTP Server (xử lý /summarize & /chat)
│   └── requirements.txt                # Thư viện Python phụ thuộc
└── android-client/                     # Mã nguồn ứng dụng Android (Kotlin + Jetpack Compose)
    └── app/src/main/
        ├── AndroidManifest.xml         # Quyền INTERNET, CLEAR_TEXT & Network Security
        ├── res/xml/
        │   └── network_security_config.xml # Cấu hình bảo mật mạng
        └── java/com/thanhbinh/englishaiapp/
            ├── data/
            │   ├── model/ChatMessage.kt # Model dữ liệu tin nhắn Chat
            │   └── local/              # Room Database, DAOs & Entities
            ├── presentation/
            │   ├── components/
            │   │   ├── BottomNavigationBar.kt
            │   │   └── ServerConfigDialog.kt # Hộp thoại chuyển đổi USB / LAN IP
            │   ├── navigation/         # Điều hướng màn hình (NavGraph, Screen)
            │   ├── screens/
            │   │   ├── HomeScreen/     # Màn hình chính
            │   │   ├── Screens_Chat/   # Màn hình Chat AI (ChatAIScreen)
            │   │   └── Screens_Scan/   # Màn hình Quét & Tóm tắt AI (ScanResultScreen)
            │   └── viewmodel/
            │       ├── chat/ChatViewModel.kt       # ViewModel xử lý hội thoại AI
            │       └── scan/ScanResultViewModel.kt # ViewModel xử lý tóm tắt AI
            └── utils/
                └── AppConfig.kt        # Cấu hình IP Server, Port, Timeouts & Auto-fallback
```

---

## 💻 Công nghệ sử dụng

- **Frontend Android**: Kotlin, Jetpack Compose, Material 3, Navigation Compose, Coroutines & StateFlow.
- **Local Database & Network**: Room DB, OkHttpClient (hỗ trợ Auto-fallback USB / LAN, 10s timeout).
- **Backend & AI Gateway**: Python (Flask / http.server).
- **AI Core**: Ollama running **Llama 3.1** (Meta AI) / Llama 3.2.
