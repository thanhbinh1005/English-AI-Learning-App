#!/usr/bin/env python3
"""
EnglishAIApp Backend Server (Llama 3.1 + Ollama)
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
OLLAMA_MODEL = os.environ.get("OLLAMA_MODEL", "llama3.1")
PORT = int(os.environ.get("PORT", 5000))

def call_ollama(prompt: str) -> dict:
    """Gui yeu cau den Ollama API su dung thu vien chuan urllib."""
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
        "Ban la mot chuyen gia ngon ngu AI chuyen phan tich va tom tat van ban. "
        "Hay doc van ban duoi day va tom tat thanh cac luan diem chinh ngan gon, suc tich, de hieu. "
        "Trinh bay ket qua bang cac gach dau dong ro rang bang tieng Viet.\n\n"
        f"Van ban can tom tat:\n{text}"
    )
    try:
        logger.info(f"Dang gui van ban toi Ollama model: {OLLAMA_MODEL}")
        result = call_ollama(prompt)
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
            "TOM TAT LUAN DIEM CHINH (AI Summary):\n\n"
            f"{bullet_points}\n\n"
            "----------------------------------------\n"
            "(Ghi chu: Khoi dong Ollama bang lenh 'ollama run llama3.1' de nhan phan tich truc tiep tu mo hinh Llama 3.1)"
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
        logger.info(f"Dang gui cau hoi chat toi Ollama model: {OLLAMA_MODEL}")
        result = call_ollama(prompt)
        response_text = result.get("response", "").strip()
        if response_text:
            return response_text
        raise ValueError("Ollama tra ve phan hoi rong")
    except Exception as e:
        logger.warning(f"Khong the ket noi toi Ollama tai {OLLAMA_API_URL} ({e}). Su dung phan hoi gia su du phong...")
        lower_msg = message.lower().strip()
        
        if "hiện tại đơn" in lower_msg or "present simple" in lower_msg:
            return (
                "THI HIEN TAI DON (Present Simple Tense):\n\n"
                "1. Cong thuc:\n"
                "   - Khang dinh: S + V(s/es) (Vi du: She works hard every day)\n"
                "   - Phu dinh: S + do/does + not + V_inf (Vi du: I do not like coffee)\n"
                "   - Nghi van: Do/Does + S + V_inf? (Vi du: Do you speak English?)\n\n"
                "2. Cach dung:\n"
                "   - Dien ta chan ly, su that hien nhien (The sun rises in the east).\n"
                "   - Dien ta thoi quen, hanh dong lap di lap lai (I brush my teeth twice a day).\n"
                "   - Dien ta lich trinh, thoi gian bieu co dinh (The train leaves at 8 PM).\n\n"
                "3. Dau hieu nhan biet: always, usually, often, sometimes, never, every day/week..."
            )
        elif "từ vựng" in lower_msg or "vocabulary" in lower_msg or "5 từ" in lower_msg:
            return (
                "5 TU VUNG TIENG ANH GIAO TIEP HANG NGAY:\n\n"
                "1. Accomplish /əˈkʌm.plɪʃ/ (v): Hoan thanh, dat duoc muc tieu.\n"
                "   Vi du: We can accomplish this goal together.\n\n"
                "2. Persistent /pəˈsɪs.tənt/ (adj): Kien tri, ben bi.\n"
                "   Vi du: Practice makes perfect if you are persistent.\n\n"
                "3. Fluency /ˈfluː.ən.si/ (n): Su luu loat, troi chay.\n"
                "   Vi du: Daily speaking practice boosts your fluency.\n\n"
                "4. Valuable /ˈvæl.jə.bəl/ (adj): Quy gia, co gia tri.\n"
                "   Vi du: Thank you for your valuable advice.\n\n"
                "5. Effortless /ˈef.ət.ləs/ (adj): De dang, tu nhien.\n"
                "   Vi du: Speaking English will feel effortless with regular practice."
            )
        elif "dạy" in lower_msg or "học" in lower_msg or "tiếng anh" in lower_msg:
            return (
                "Chao ban! Tro ly AI san sang ho tro ban hoc tieng Anh.\n\n"
                "Cac chu de ho tro chinh:\n"
                "1. Ngu phap: 12 thi trong tieng Anh, cau dieu kien, cau bi dong, menh de quan he.\n"
                "2. Tu vung: Tu vung giao tiep, luyen thi IELTS, TOEIC.\n"
                "3. Sua loi cau: Sua ngu phap va cach dien dat trong cau.\n"
                "4. Luyen hoi thoai: Tro chuyen theo cac tinh huong thuc te.\n\n"
                "Ban co the bat dau bang cach gui cau hoi hoac cau can sua."
            )
        else:
            return (
                f"Phan hoi cho cau hoi: \"{message}\"\n\n"
                "Tro ly AI san sang giai dap thac mac ve ngu phap, tu vung va luyen tap tieng Anh.\n\n"
                "(Luu y: De kich hoat toan bo mo hinh suy luan Llama 3.1 truc tiep, hay khoi dong Ollama bang lenh: ollama run llama3.1)"
            )

# --- Che do 1: Su dung Flask neu da cai dat ---
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
                return jsonify({"error": "Van ban rong"}), 400

            logger.info(f"[Flask] Nhan yeu cau tom tat van ban ({len(text)} ky tu)")
            summary = handle_summarize(text)
            return jsonify({"summary": summary, "status": "success"}), 200
        except Exception as e:
            logger.error(f"Loi: {e}")
            return jsonify({"error": str(e)}), 500

    @app.route("/chat", methods=["POST"])
    def chat():
        try:
            data = request.get_json(force=True, silent=True) or {}
            message = data.get("message", "").strip()
            if not message:
                return jsonify({"error": "Tin nhan rong"}), 400

            logger.info(f"[Flask] Nhan cau hoi Chat AI: '{message}'")
            reply = handle_chat(message)
            return jsonify({"reply": reply, "status": "success"}), 200
        except Exception as e:
            logger.error(f"Loi: {e}")
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
                    self._send_json(400, {"error": "Van ban rong"})
                    return
                logger.info(f"[Server] Nhan yeu cau tom tat van ban ({len(text)} ky tu)")
                summary = handle_summarize(text)
                self._send_json(200, {"summary": summary, "status": "success"})

            elif self.path == "/chat":
                message = data.get("message", "").strip()
                if not message:
                    self._send_json(400, {"error": "Tin nhan rong"})
                    return
                logger.info(f"[Server] Nhan cau hoi Chat AI: '{message}'")
                reply = handle_chat(message)
                self._send_json(200, {"reply": reply, "status": "success"})

            else:
                self._send_json(404, {"error": f"Endpoint '{self.path}' khong ton tai"})

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
    print("EnglishAI Server (Llama 3.1 + Ollama) khoi dong...")
    print(f"Dia chi lang nghe: http://0.0.0.0:{PORT}")
    print(f"Mo hinh Ollama:    {OLLAMA_MODEL} (tai {OLLAMA_API_URL})")
    print(f"Endpoint Tom tat:  POST http://localhost:{PORT}/summarize")
    print(f"Endpoint Chat AI:  POST http://localhost:{PORT}/chat")
    print("=" * 65)

    try:
        import flask
        logger.info("Phat hien Flask da cai dat. Chay server bang Flask...")
        run_flask()
    except ImportError:
        logger.info("Flask chua duoc cai dat. Tu dong chuyen sang Python Built-in HTTP Server...")
        run_builtin_server()
