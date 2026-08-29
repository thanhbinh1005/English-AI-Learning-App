# English AI Learning App

Ứng dụng hỗ trợ học tiếng Anh thông minh được tích hợp mô hình ngôn ngữ lớn (Llama 3.1), kết hợp công nghệ nhận diện ký tự quang học (OCR) và hệ thống quản lý học tập cá nhân hóa.

---

## 1. Tổng quan hệ thống

English AI Learning App là một ứng dụng di động Android kết hợp với máy chủ xử lý cục bộ (Local Backend Server) và mô hình Trí tuệ Nhân tạo chạy trên Ollama. Hệ thống cung cấp giải pháp toàn diện cho việc tự học tiếng Anh thông qua tương tác đối thoại thông minh và cô đọng tài liệu học tập.

### Các tính năng chính

- **Trợ lý Chat AI Ngôn ngữ**: Hỗ trợ hỏi đáp ngữ pháp, giải thích từ vựng, sửa lỗi diễn đạt và luyện tập hội thoại tiếng Anh thời gian thực.
- **Tóm tắt văn bản bằng AI**: Phân tích cú pháp và trích xuất các luận điểm chính từ các đoạn văn bản dài hoặc tài liệu được quét.
- **Quét tài liệu & OCR**: Chụp ảnh hoặc tải lên tài liệu hình ảnh để trích xuất văn bản tự động.
- **Học từ vựng & Flashcards**: Tổ chức từ vựng theo thư mục và hỗ trợ ôn tập ngắt quãng.
- **Dịch thuật**: Hỗ trợ dịch nhanh nội dung văn bản.

---

## 2. Hướng dẫn Cài đặt & Cấu hình Mô hình AI (Ollama + Llama 3.1)

> **Thành viên thực hiện**: Nguyễn Đăng Khôi  
> **Phạm vi chức năng**:
> - Use case: "Chat với AI" (Trợ lý ngôn ngữ tương tác với Llama 3.1)
> - Use case: "Tóm tắt văn bản bằng AI" (Trích xuất luận điểm tự động)

Hệ thống AI được thiết kế để chạy trực tiếp trên máy tính cá nhân thông qua **Ollama**, kết nối với ứng dụng Android qua **Server trung gian (Python)**.

---

### Bước 1: Cài đặt Ollama

#### Trên Linux / Ubuntu / WSL
Thực thi lệnh cài đặt tự động từ trang chủ Ollama:
```bash
curl -fsSL https://ollama.com/install.sh | sh
```

#### Trên Windows / macOS
- Tải bộ cài đặt chính thức tại: https://ollama.com/download
- Tiến hành cài đặt theo hướng dẫn của hệ điều hành.

---

### Bước 2: Tải và khởi chạy mô hình Llama 3.1

Mở Terminal trên máy tính và thực thi:

```bash
ollama run llama3.1
```

#### Lưu ý về yêu cầu phần cứng và mô hình thay thế:
- **Cấu hình tiêu chuẩn (RAM >= 8GB - 16GB)**: Khuyên dùng `llama3.1` (dung lượng xấp xỉ 4.7 GB).
- **Cấu hình tối giản (RAM < 8GB)**: Có thể sử dụng mô hình `llama3.2` (dung lượng xấp xỉ 2.0 GB, tốc độ xử lý nhanh):
  ```bash
  ollama run llama3.2
  ```
  *(Khi sử dụng `llama3.2`, thiết lập biến môi trường trước khi chạy server: `export OLLAMA_MODEL="llama3.2"`)*.

Kiểm tra trạng thái hoạt động của dịch vụ Ollama tại cổng `11434`:
```bash
curl http://localhost:11434/api/tags
```

---

### Bước 3: Khởi chạy Backend Server trung gian

Backend Server tiếp nhận yêu cầu từ ứng dụng Android tại các endpoint `/chat` và `/summarize`, sau đó chuyển tiếp dữ liệu đến Ollama để xử lý.

1. Di chuyển vào thư mục chứa server:
   ```bash
   cd server
   ```
2. Khởi chạy server:
   ```bash
   python3 app.py
   ```

Server được tích hợp sẵn cơ chế tương thích, có thể chạy trực tiếp bằng thư viện chuẩn của Python hoặc thông qua Flask.

Thông tin hiển thị khi khởi động thành công:
```text
=================================================================
EnglishAI Server (Llama 3.1 + Ollama) khoi dong...
Dia chi lang nghe: http://0.0.0.0:5000
Mo hinh Ollama:    llama3.1 (tai http://localhost:11434/api/generate)
Endpoint Tom tat:  POST http://localhost:5000/summarize
Endpoint Chat AI:  POST http://localhost:5000/chat
=================================================================
```

---

### Bước 4: Thiết lập kết nối giữa Điện thoại Android và Máy tính

Ứng dụng hỗ trợ hai phương thức kết nối:

#### Phương thức 1: Kết nối qua cáp USB (ADB Reverse Port Forwarding - Khuyên dùng)
1. Kết nối điện thoại với máy tính qua cáp USB và kích hoạt chế độ **USB Debugging (Gỡ lỗi USB)** trên điện thoại.
2. Trên máy tính, mở Terminal và chạy lệnh chuyển tiếp cổng:
   ```bash
   adb reverse tcp:5000 tcp:5000
   ```
3. Mở ứng dụng Android, hệ thống sẽ tự động giao tiếp với máy tính qua địa chỉ `127.0.0.1:5000`.

#### Phương thức 2: Kết nối qua mạng Wi-Fi LAN
1. Đảm bảo điện thoại và máy tính kết nối vào cùng một mạng Wi-Fi.
2. Lấy địa chỉ IPv4 nội bộ của máy tính:
   - **Linux**: Chạy lệnh `hostname -I` (lấy địa chỉ IP đầu tiên).
   - **Windows**: Mở Command Prompt, chạy `ipconfig` và xem giá trị `IPv4 Address`.
   - **macOS**: Chạy lệnh `ipconfig getifaddr en0`.
3. Cấu hình trên ứng dụng Android:
   - Mở màn hình **Chat AI** hoặc **Chi tiết văn bản (Tóm tắt AI)**.
   - Nhấn vào biểu tượng **Cài đặt Server** trên thanh tiêu đề.
   - Nhập địa chỉ IPv4 của máy tính (ví dụ: `192.168.1.50`), nhấn **Kiểm tra** và bấm **Lưu & Áp dụng**.

---

## 3. Hướng dẫn Kiểm thử & Vận hành 2 Use Case

### Use case 1: "Chat với AI" (Language Tutor Assistant)
1. Tại màn hình chính, chọn mục **Trợ lý Chat AI** (hoặc chọn tab **Chat AI** trên thanh điều hướng phía dưới).
2. Nhập câu hỏi vào khung nhập liệu (ví dụ: *"hiện tại đơn là thì như nào"*, *"dạy mình 5 từ vựng giao tiếp"*, *"sửa lỗi câu: I goes to school"*), hoặc chọn các thẻ câu hỏi gợi ý nhanh.
3. Tin nhắn của người dùng hiển thị ở bong bóng phía bên phải.
4. Hệ thống kích hoạt hiệu ứng chờ xử lý trong thời gian mô hình suy luận.
5. Phản hồi từ mô hình Llama 3.1 hiển thị tại bong bóng phía bên trái, danh sách tự động cuộn xuống dưới cùng.
6. Người dùng có thể chạm vào tin nhắn để sao chép nội dung vào bộ nhớ tạm.

### Use case 2: "Tóm tắt văn bản bằng AI" (Text Summarization)
1. Chọn tab **Quét** trên thanh điều hướng phía dưới.
2. Nhấn nút **Quét tài liệu mới** (để trích xuất từ camera/ảnh) hoặc nhấn **Mở văn bản mẫu thử Tóm tắt AI** / nhấn **Xem** trên một tài liệu đã lưu.
3. Tại màn hình **Chi tiết văn bản**, nhấn vào tab **Tóm tắt AI**.
4. Ứng dụng hiển thị vòng xoay tải dữ liệu và gửi yêu cầu `POST /summarize` kèm nội dung văn bản thô lên server.
5. Mô hình Llama 3.1 phân tích ngữ nghĩa và trả về danh sách các luận điểm chính ngắn gọn, trực quan.

---

## 4. Cấu trúc thư mục dự án

```text
English-AI-Learning-App/
├── README.md                           # Tài liệu kỹ thuật dự án
├── server/                             # Backend Server trung gian
│   ├── app.py                          # HTTP Server xử lý /summarize và /chat
│   └── requirements.txt                # Danh sách thư viện Python phụ thuộc
└── android-client/                     # Ứng dụng Android (Kotlin + Jetpack Compose)
    └── app/src/main/
        ├── AndroidManifest.xml         # Khai báo quyền Internet và cấu hình bảo mật
        ├── res/xml/
        │   └── network_security_config.xml # Cấu hình cho phép kết nối mạng nội bộ
        └── java/com/thanhbinh/englishaiapp/
            ├── data/
            │   ├── model/ChatMessage.kt # Data class biểu diễn tin nhắn trò chuyện
            │   └── local/              # Quản trị cơ sở dữ liệu Room (Entity, DAO, DB)
            ├── presentation/
            │   ├── components/
            │   │   ├── BottomNavigationBar.kt # Thanh điều hướng chính
            │   │   └── ServerConfigDialog.kt  # Hộp thoại cấu hình IP kết nối linh hoạt
            │   ├── navigation/         # Điều hướng màn hình (NavGraph, Screen)
            │   ├── screens/
            │   │   ├── HomeScreen/     # Màn hình trang chủ
            │   │   ├── Screens_Chat/   # Giao diện Chat AI (ChatAIScreen)
            │   │   └── Screens_Scan/   # Giao diện Quét tài liệu & Tóm tắt AI
            │   └── viewmodel/
            │       ├── chat/ChatViewModel.kt       # Xử lý logic và trạng thái hội thoại
            │       └── scan/ScanResultViewModel.kt # Xử lý logic và trạng thái tóm tắt
            └── utils/
                └── AppConfig.kt        # Cấu hình IP, Port, Timeout và Auto-fallback
```

---

## 5. Công nghệ & Thư viện sử dụng

- **Frontend Android**: Kotlin 2.1, Jetpack Compose, Material 3, Navigation Compose, Coroutines, StateFlow.
- **Lưu trữ & Mạng**: Room Database, OkHttp 4.12 (hỗ trợ Auto-fallback USB / LAN, timeout 10 giây).
- **Backend Gateway**: Python 3 (tương thích Flask và Built-in HTTP Server).
- **AI Core & Mô hình**: Ollama, Meta Llama 3.1 (8B) / Llama 3.2 (3B).
