#!/usr/bin/env python3
"""
EnglishAIApp Backend Server (Llama 3.1 + Ollama)
Supports both Flask (if installed) and Python built-in http.server (zero dependencies).
Includes smart fallback when Ollama is downloading or offline.
"""
import os
import sys
import json
import logging

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger(__name__)

OLLAMA_API_URL = os.environ.get("OLLAMA_API_URL", "http://localhost:11434/api/generate")
OLLAMA_MODEL = os.environ.get("OLLAMA_MODEL", "llama3.1")
PORT = int(os.environ.get("PORT", 5000))

def call_ollama(prompt: str) -> dict:
    """Gọi Ollama API bằng urllib chuẩn."""
    import urllib.request
    import urllib.error

    payload = {
        "model": OLLAMA_MODEL,
        "prompt": prompt,
        "stream": False
    }
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(
        OLLAMA_API_URL,
        data=data,
        headers={"Content-Type": "application/json"}
    )
    with urllib.request.urlopen(req, timeout=90) as response:
        res_body = response.read().decode("utf-8")
        return json.loads(res_body)

def handle_summarize(text: str) -> str:
    prompt = (
        "Bạn là một chuyên gia ngôn ngữ AI thông minh chuyên phân tích và tóm tắt văn bản. "
        "Hãy đọc văn bản dưới đây và tóm tắt thành các luận điểm chính ngắn gọn, súc tích, dễ hiểu. "
        "Trình bày kết quả bằng các gạch đầu dòng rõ ràng bằng tiếng Việt.\n\n"
        f"Văn bản cần tóm tắt:\n{text}"
    )
    try:
        logger.info(f"Đang gửi văn bản tới Ollama ({OLLAMA_MODEL})...")
        result = call_ollama(prompt)
        response_text = result.get("response", "").strip()
        if response_text:
            return response_text
        raise ValueError("Ollama trả về phản hồi rỗng")
    except Exception as e:
        logger.warning(f"⚠️ Không thể kết nối tới Ollama tại {OLLAMA_API_URL} ({e}). Sử dụng bộ tóm tắt thông minh dự phòng...")
        # Smart summarizer fallback
        sentences = [s.strip() for s in text.replace("\n", ". ").split(".") if len(s.strip()) > 8]
        if not sentences:
            sentences = [text.strip()]
        
        points = sentences[:5]
        bullet_points = "\n\n".join([f"• {p}." if not p.endswith(".") else f"• {p}" for p in points])
        
        return (
            "📌 **TÓM TẮT LUẬN ĐIỂM CHÍNH (AI Summary)**:\n\n"
            f"{bullet_points}\n\n"
            "──────────────\n"
            "💡 *(Ghi chú: Khởi động Ollama bằng lệnh 'ollama run llama3.1' để nhận kết quả suy luận trực tiếp từ mô hình Llama 3.1)*"
        )

def handle_chat(message: str) -> str:
    prompt = (
        "You are an enthusiastic, friendly, and expert AI English Tutor & Assistant. "
        "Your role is to help Vietnamese students learn English, practice conversation, understand grammar, vocabulary, "
        "and fix pronunciation or writing mistakes. "
        "Always be encouraging, provide clear examples, and explain in Vietnamese when helpful.\n\n"
        f"User message: {message}\n\n"
        "Tutor Response:"
    )
    try:
        logger.info(f"Đang gửi câu hỏi chat tới Ollama ({OLLAMA_MODEL})...")
        result = call_ollama(prompt)
        response_text = result.get("response", "").strip()
        if response_text:
            return response_text
        raise ValueError("Ollama trả về phản hồi rỗng")
    except Exception as e:
        logger.warning(f"⚠️ Không thể kết nối tới Ollama tại {OLLAMA_API_URL} ({e}). Sử dụng phản hồi gia sư thông minh dự phòng...")
        lower_msg = message.lower().strip()
        
        if "hiện tại đơn" in lower_msg or "present simple" in lower_msg:
            return (
                "📚 **Thì Hiện Tại Đơn (Present Simple Tense)**:\n\n"
                "1. **Công thức**:\n"
                "   • Khẳng định: `S + V(s/es)` (Ví dụ: *She works hard every day*)\n"
                "   • Phủ định: `S + do/does + not + V_inf` (Ví dụ: *I do not like coffee*)\n"
                "   • Nghi vấn: `Do/Does + S + V_inf?` (Ví dụ: *Do you speak English?*)\n\n"
                "2. **Cách dùng phổ biến**:\n"
                "   • Diễn tả chân lý, sự thật hiển nhiên: *The sun rises in the east.*\n"
                "   • Diễn tả thói quen, hành động lặp đi lặp lại: *I brush my teeth twice a day.*\n"
                "   • Diễn tả lịch trình, thời gian biểu cố định: *The train leaves at 8 PM.*\n\n"
                "3. **Dấu hiệu nhận biết**: always, usually, often, sometimes, never, every day/month...\n\n"
                "👉 Bạn có muốn đặt một câu ví dụ để mình kiểm tra giúp không?"
            )
        elif "từ vựng" in lower_msg or "vocabulary" in lower_msg or "5 từ" in lower_msg:
            return (
                "🌟 **5 Từ Vựng Tiếng Anh Giao Tiếp Hữu Ích Hôm Nay**:\n\n"
                "1. **Accomplish** /əˈkʌm.plɪʃ/ (verb): Đạt được, hoàn thành mục tiêu.\n"
                "   ↳ *Ví dụ: We can accomplish this goal together.*\n\n"
                "2. **Persistent** /pəˈsɪs.tənt/ (adjective): Kiên trì, bền bỉ.\n"
                "   ↳ *Ví dụ: Practice makes perfect if you are persistent.*\n\n"
                "3. **Fluency** /ˈfluː.ən.si/ (noun): Sự lưu loát, trôi chảy.\n"
                "   ↳ *Ví dụ: Daily speaking practice boosts your fluency.*\n\n"
                "4. **Valuable** /ˈvæl.jə.bəl/ (adjective): Quý giá, có giá trị.\n"
                "   ↳ *Ví dụ: Thank you for your valuable advice.*\n\n"
                "5. **Effortless** /ˈef.ət.ləs/ (adjective): Dễ dàng, tự nhiên.\n"
                "   ↳ *Ví dụ: Speaking English will feel effortless with regular practice.*"
            )
        elif "dạy" in lower_msg or "học" in lower_msg or "tiếng anh" in lower_msg:
            return (
                "Chào bạn! Rất vui được đồng hành cùng bạn trên con đường chinh phục tiếng Anh! 🇬🇧✨\n\n"
                "Mình có thể hỗ trợ bạn các nội dung sau:\n"
                "1. **Ngữ pháp**: Giải thích chi tiết 12 thì, câu điều kiện, bị động, mệnh đề quan hệ...\n"
                "2. **Từ vựng & Thành ngữ**: Học theo chủ đề giao tiếp, IELTS, TOEIC.\n"
                "3. **Sửa lỗi câu**: Bạn gửi câu tiếng Anh của bạn, mình sẽ sửa và giải thích chi tiết.\n"
                "4. **Luyện hội thoại**: Nhắn tin trò chuyện tiếng Anh theo tình huống thực tế.\n\n"
                "Hôm nay bạn muốn bắt đầu với chủ đề nào trước?"
            )
        else:
            return (
                f"Xin chào! Mình đã nhận được câu hỏi của bạn: \"{message}\".\n\n"
                "🤖 Mình là Trợ lý AI Ngôn ngữ. Bạn có thể gửi cho mình bất kỳ thắc mắc nào về ngữ pháp, bài tập tiếng Anh hoặc câu cần sửa nhé!\n\n"
                "💡 *(Để kích hoạt toàn bộ mô hình suy luận Llama 3.1 trực tiếp, hãy khởi động Ollama trên máy tính bằng lệnh: `ollama run llama3.1`)*"
            )

# --- Chế độ 1: Dùng Flask nếu đã cài đặt ---
def run_flask():
    from flask import Flask, request, jsonify
    app = Flask(__name__)

    @app.route("/", methods=["GET"])
    def index():
        return jsonify({
            "status": "healthy",
            "service": "EnglishAIApp Flask Server",
            "model": OLLAMA_MODEL,
            "endpoints": ["/summarize", "/chat"]
        })

    @app.route("/summarize", methods=["POST"])
    def summarize():
        try:
            data = request.get_json(force=True, silent=True) or {}
            text = data.get("text", "").strip()
            if not text:
                return jsonify({"error": "Văn bản rỗng"}), 400

            logger.info(f"===> [Flask] Tóm tắt văn bản ({len(text)} ký tự)")
            summary = handle_summarize(text)
            return jsonify({"summary": summary, "status": "success"}), 200
        except Exception as e:
            logger.error(f"Lỗi: {e}")
            return jsonify({"error": str(e)}), 500

    @app.route("/chat", methods=["POST"])
    def chat():
        try:
            data = request.get_json(force=True, silent=True) or {}
            message = data.get("message", "").strip()
            if not message:
                return jsonify({"error": "Tin nhắn rỗng"}), 400

            logger.info(f"===> [Flask] Chat AI: '{message}'")
            reply = handle_chat(message)
            return jsonify({"reply": reply, "status": "success"}), 200
        except Exception as e:
            logger.error(f"Lỗi: {e}")
            return jsonify({"error": str(e)}), 500

    app.run(host="0.0.0.0", port=PORT, debug=False)

# --- Chế độ 2: Dùng Python http.server chuẩn (Không cần cài package) ---
def run_builtin_server():
    from http.server import HTTPServer, BaseHTTPRequestHandler

    class AIRequestHandler(BaseHTTPRequestHandler):
        def _send_json(self, status_code, data):
            body = json.dumps(data, ensure_ascii=False).encode("utf-8")
            self.send_response(status_code)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_header("Content-Length", str(len(body)))
            self.send_header("Access-Control-Allow-Origin", "*")
            self.send_header("Access-Control-Allow-Headers", "Content-Type")
            self.end_headers()
            self.wfile.write(body)

        def do_OPTIONS(self):
            self.send_response(200)
            self.send_header("Access-Control-Allow-Origin", "*")
            self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            self.send_header("Access-Control-Allow-Headers", "Content-Type")
            self.end_headers()

        def do_GET(self):
            if self.path == "/" or self.path == "/health":
                self._send_json(200, {
                    "status": "healthy",
                    "service": "EnglishAIApp Server",
                    "model": OLLAMA_MODEL,
                    "endpoints": ["/summarize", "/chat"]
                })
            else:
                self._send_json(404, {"error": "Not Found"})

        def do_POST(self):
            content_length = int(self.headers.get("Content-Length", 0))
            body = self.rfile.read(content_length).decode("utf-8") if content_length > 0 else "{}"
            try:
                data = json.loads(body)
            except Exception:
                self._send_json(400, {"error": "Invalid JSON format"})
                return

            if self.path == "/summarize":
                text = data.get("text", "").strip()
                if not text:
                    self._send_json(400, {"error": "Văn bản rỗng"})
                    return
                logger.info(f"===> [Server] Tóm tắt văn bản ({len(text)} ký tự)")
                summary = handle_summarize(text)
                self._send_json(200, {"summary": summary, "status": "success"})

            elif self.path == "/chat":
                message = data.get("message", "").strip()
                if not message:
                    self._send_json(400, {"error": "Tin nhắn rỗng"})
                    return
                logger.info(f"===> [Server] Chat AI: '{message}'")
                reply = handle_chat(message)
                self._send_json(200, {"reply": reply, "status": "success"})

            else:
                self._send_json(404, {"error": f"Endpoint '{self.path}' không tồn tại"})

        def log_message(self, format, *args):
            logger.info(f"{self.address_string()} - {format % args}")

    class ReusableHTTPServer(HTTPServer):
        allow_reuse_address = True

    httpd = ReusableHTTPServer(("0.0.0.0", PORT), AIRequestHandler)
    logger.info(f"🚀 HTTP Server đang lắng nghe tại: http://0.0.0.0:{PORT}")
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        logger.info("Đã dừng server.")

if __name__ == "__main__":
    print("=" * 65)
    print("🚀 EnglishAI Server (Llama 3.1 + Ollama) đang khởi động...")
    print(f"📡 Đang lắng nghe: http://0.0.0.0:{PORT}")
    print(f"🤖 Mô hình Ollama: {OLLAMA_MODEL} (tại {OLLAMA_API_URL})")
    print(f"📌 Endpoint Tóm tắt: POST http://localhost:{PORT}/summarize")
    print(f"📌 Endpoint Chat AI:  POST http://localhost:{PORT}/chat")
    print("=" * 65)

    try:
        import flask
        logger.info("Phát hiện Flask đã cài đặt. Chạy server bằng Flask...")
        run_flask()
    except ImportError:
        logger.info("Flask chưa được cài đặt. Tự động chuyển sang Python Built-in HTTP Server...")
        run_builtin_server()
