#!/usr/bin/env python3
"""
EnglishAIApp Backend Server (Llama 3.2 + Ollama)
Supports Flask and Python standard http.server.
Includes fallback responses when Ollama service is unavailable.
"""
import os
import sys
import json
import logging

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger(__name__)

OLLAMA_API_URL = os.environ.get("OLLAMA_API_URL", "http://localhost:11434/api/generate")
OLLAMA_MODEL = os.environ.get("OLLAMA_MODEL", "llama3.2")
PORT = int(os.environ.get("PORT", 5000))

def call_ollama(prompt: str, system: str = None, temperature: float = 0.5) -> dict:
    """Gui yeu cau den Ollama API su dung thu vien chuan urllib."""
    import urllib.request
    import urllib.error

    payload = {
        "model": OLLAMA_MODEL,
        "prompt": prompt,
        "stream": False,
        "options": {
            "temperature": temperature
        }
    }
    if system:
        payload["system"] = system

    data = json.dumps(payload, ensure_ascii=False).encode("utf-8")
    req = urllib.request.Request(
        OLLAMA_API_URL,
        data=data,
        headers={"Content-Type": "application/json; charset=utf-8"}
    )
    with urllib.request.urlopen(req, timeout=90) as response:
        res_body = response.read().decode("utf-8")
        return json.loads(res_body)

def handle_summarize(text: str) -> str:
    system_prompt = (
        "Bạn là một chuyên gia ngôn ngữ AI hàng đầu, chuyên phân tích và tóm tắt văn bản. "
        "Nhiệm vụ của bạn là đọc kỹ văn bản được cung cấp và tạo bản tóm tắt ngắn gọn, cô đọng các luận điểm chính bằng tiếng Việt chuẩn xác. "
        "Trình bày kết quả dưới dạng danh sách gạch đầu dòng rõ ràng, dễ đọc."
    )
    user_prompt = f"Vui lòng đọc kỹ văn bản sau và tóm tắt thành các luận điểm chính:\n\n{text}\n\nBản tóm tắt (tiếng Việt):"
    
    try:
        logger.info(f"Dang gui van ban toi Ollama model: {OLLAMA_MODEL}")
        result = call_ollama(prompt=user_prompt, system=system_prompt, temperature=0.3)
        response_text = result.get("response", "").strip()
        if response_text:
            return response_text
        raise ValueError("Ollama tra ve phan hoi rong")
    except Exception as e:
        logger.warning(f"Khong the ket noi toi Ollama tai {OLLAMA_API_URL} ({e}). Su dung bo tom tat du phong...")
        sentences = [s.strip() for s in text.replace("\n", ". ").split(".") if len(s.strip()) > 8]
        if not sentences:
            sentences = [text.strip()]
        
        points = sentences[:5]
        bullet_points = "\n\n".join([f"- {p}." if not p.endswith(".") else f"- {p}" for p in points])
        
        return (
            "TÓM TẮT LUẬN ĐIỂM CHÍNH (AI Summary Backup):\n\n"
            f"{bullet_points}\n\n"
            "----------------------------------------\n"
            "(Ghi chú: Khởi động Ollama bằng lệnh 'ollama run llama3.2' để nhận phân tích trực tiếp từ mô hình Llama 3.2)"
        )

def handle_chat(message: str) -> str:
    system_prompt = (
        "Bạn là một Trợ lý AI dạy Tiếng Anh thân thiện, nhiệt tình và giàu kinh nghiệm. "
        "Nhiệm vụ của bạn là hỗ trợ học sinh Việt Nam học tiếng Anh, luyện giao tiếp, giải thích ngữ pháp, từ vựng "
        "và sửa lỗi phát âm hoặc kỹ năng viết. Always encourage students, provide clear examples, and explain in Vietnamese when helpful."
    )
    user_prompt = f"User message: {message}\n\nTutor Response:"

    try:
        logger.info(f"Dang gui cau hoi chat toi Ollama model: {OLLAMA_MODEL}")
        result = call_ollama(prompt=user_prompt, system=system_prompt, temperature=0.7)
        response_text = result.get("response", "").strip()
        if response_text:
            return response_text
        raise ValueError("Ollama tra ve phan hoi rong")
    except Exception as e:
        logger.warning(f"Khong the ket noi toi Ollama tai {OLLAMA_API_URL} ({e}). Su dung phan hoi gia su du phong...")
        lower_msg = message.lower().strip()
        
        if "hiện tại đơn" in lower_msg or "present simple" in lower_msg:
            return (
                "THÌ HIỆN TẠI ĐƠN (Present Simple Tense):\n\n"
                "1. Công thức:\n"
                "   - Khẳng định: S + V(s/es) (Ví dụ: She works hard every day)\n"
                "   - Phủ định: S + do/does + not + V_inf (Ví dụ: I do not like coffee)\n"
                "   - Nghi vấn: Do/Does + S + V_inf? (Ví dụ: Do you speak English?)\n\n"
                "2. Cách dùng:\n"
                "   - Diễn tả chân lý, sự thật hiển nhiên (The sun rises in the east).\n"
                "   - Diễn tả thói quen, hành động lặp đi lặp lại (I brush my teeth twice a day).\n"
                "   - Diễn tả lịch trình, thời gian biểu cố định (The train leaves at 8 PM).\n\n"
                "3. Dấu hiệu nhận biết: always, usually, often, sometimes, never, every day/week..."
            )
        elif "từ vựng" in lower_msg or "vocabulary" in lower_msg or "5 từ" in lower_msg:
            return (
                "5 TỪ VỰNG TIẾNG ANH GIAO TIẾP HÀNG NGÀY:\n\n"
                "1. Accomplish /əˈkʌm.plɪʃ/ (v): Hoàn thành, đạt được mục tiêu.\n"
                "   Ví dụ: We can accomplish this goal together.\n\n"
                "2. Persistent /pəˈsɪs.tənt/ (adj): Kiên trì, bền bỉ.\n"
                "   Ví dụ: Practice makes perfect if you are persistent.\n\n"
                "3. Fluency /ˈfluː.ən.si/ (n): Sự lưu khoát, trôi chảy.\n"
                "   Ví dụ: Daily speaking practice boosts your fluency.\n\n"
                "4. Valuable /ˈvæl.jə.bəl/ (adj): Quý giá, có giá trị.\n"
                "   Ví dụ: Thank you for your valuable advice.\n\n"
                "5. Effortless /ˈef.ət.les/ (adj): Dễ dàng, tự nhiên.\n"
                "   Ví dụ: Speaking English will feel effortless with regular practice."
            )
        elif "dạy" in lower_msg or "học" in lower_msg or "tiếng anh" in lower_msg:
            return (
                "Chào bạn! Trợ lý AI sẵn sàng hỗ trợ bạn học tiếng Anh.\n\n"
                "Các chủ đề hỗ trợ chính:\n"
                "1. Ngữ pháp: 12 thì trong tiếng Anh, câu điều kiện, câu bị động, mệnh đề quan hệ.\n"
                "2. Từ vựng: Từ vựng giao tiếp, luyện thi IELTS, TOEIC.\n"
                "3. Sửa lỗi câu: Sửa ngữ pháp và cách diễn đạt trong câu.\n"
                "4. Luyện hội thoại: Trò chuyện theo các tình huống thực tế.\n\n"
                "Bạn có thể bắt đầu bằng cách gửi câu hỏi hoặc câu cần sửa."
            )
        else:
            return (
                f"Phản hồi cho câu hỏi: \"{message}\"\n\n"
                "Trợ lý AI sẵn sàng giải đáp thắc mắc về ngữ pháp, từ vựng và luyện tập tiếng Anh.\n\n"
                "(Lưu ý: Để kích hoạt toàn bộ mô hình suy luận Llama 3.2 trực tiếp, hãy khởi động Ollama bằng lệnh: ollama run llama3.2)"
            )

# --- Che do 1: Su dung Flask neu da cai dat ---
def run_flask():
    from flask import Flask, request, jsonify, Response
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

            logger.info(f"[Flask] Nhận yêu cầu tóm tắt văn bản ({len(text)} ký tự)")
            summary = handle_summarize(text)
            res_data = json.dumps({"summary": summary, "status": "success"}, ensure_ascii=False)
            return Response(res_data, status=200, mimetype="application/json; charset=utf-8")
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

            logger.info(f"[Flask] Nhận câu hỏi Chat AI: '{message}'")
            reply = handle_chat(message)
            res_data = json.dumps({"reply": reply, "status": "success"}, ensure_ascii=False)
            return Response(res_data, status=200, mimetype="application/json; charset=utf-8")
        except Exception as e:
            logger.error(f"Lỗi: {e}")
            return jsonify({"error": str(e)}), 500

    app.run(host="0.0.0.0", port=PORT, debug=False)

# --- Che do 2: Su dung Python http.server chuan ---
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
                logger.info(f"[Server] Nhận yêu cầu tóm tắt văn bản ({len(text)} ký tự)")
                summary = handle_summarize(text)
                self._send_json(200, {"summary": summary, "status": "success"})

            elif self.path == "/chat":
                message = data.get("message", "").strip()
                if not message:
                    self._send_json(400, {"error": "Tin nhắn rỗng"})
                    return
                logger.info(f"[Server] Nhận câu hỏi Chat AI: '{message}'")
                reply = handle_chat(message)
                self._send_json(200, {"reply": reply, "status": "success"})

            else:
                self._send_json(404, {"error": f"Endpoint '{self.path}' không tồn tại"})

        def log_message(self, format, *args):
            logger.info(f"{self.address_string()} - {format % args}")

    class ReusableHTTPServer(HTTPServer):
        allow_reuse_address = True

    httpd = ReusableHTTPServer(("0.0.0.0", PORT), AIRequestHandler)
    logger.info(f"HTTP Server dang lang nghe tai: http://0.0.0.0:{PORT}")
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        logger.info("Da dung server.")

if __name__ == "__main__":
    print("=" * 65)
    print("EnglishAI Server (Llama 3.2 + Ollama) khởi động...")
    print(f"Địa chỉ lắng nghe: http://0.0.0.0:{PORT}")
    print(f"Mô hình Ollama:    {OLLAMA_MODEL} (tại {OLLAMA_API_URL})")
    print(f"Endpoint Tóm tắt:  POST http://localhost:{PORT}/summarize")
    print(f"Endpoint Chat AI:  POST http://localhost:{PORT}/chat")
    print("=" * 65)

    try:
        import flask
        logger.info("Phát hiện Flask đã cài đặt. Chạy server bằng Flask...")
        run_flask()
    except ImportError:
        logger.info("Flask chưa được cài đặt. Tự động chuyển sang Python Built-in HTTP Server...")
        run_builtin_server()
