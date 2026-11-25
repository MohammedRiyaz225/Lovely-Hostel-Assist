import os
import sys

# Ensure backend root is on sys.path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import uvicorn
from app.config import settings

if __name__ == "__main__":
    print("=" * 60)
    print(f"🚀 Starting {settings.APP_NAME} v{settings.APP_VERSION}")
    print(f"📡 API Base URL:       http://localhost:{settings.PORT}")
    print(f"📱 Android Emulator:   http://10.0.2.2:{settings.PORT}")
    print(f"💻 Warden Web Portal:  http://localhost:{settings.PORT}/admin")
    print(f"📚 Swagger API Docs:   http://localhost:{settings.PORT}/docs")
    print("=" * 60)
    uvicorn.run(
        "main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=settings.DEBUG
    )
