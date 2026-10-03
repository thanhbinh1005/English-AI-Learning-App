-- =================================================================
-- DỰ ÁN: ENGLISH AI LEARNING APP
-- HỆ QUẢN TRỊ CƠ SỞ DỮ LIỆU: SQLite / Room Database (Version 7)
-- Tệp: database_schema.sql
-- Vị trí: d:\HaUI\Phat_trien_UDDD\English-AI-Learning-App\database_schema.sql
-- =================================================================

PRAGMA foreign_keys = ON;

-- -----------------------------------------------------------------
-- 1. BẢNG TÀI LIỆU ĐÃ QUÉT OCR (scanned_docs)
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `scanned_docs` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `fileName` TEXT NOT NULL,
    `fileSize` TEXT NOT NULL,
    `createdAt` INTEGER NOT NULL,
    `fileType` TEXT NOT NULL,
    `filePath` TEXT NOT NULL,
    `content` TEXT NOT NULL
);

-- -----------------------------------------------------------------
-- 2. BẢNG BỘ SƯU TẬP / THƯ MỤC TỪ VỰNG (collections)
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `collections` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `name` TEXT NOT NULL,
    `description` TEXT,
    `accentColor` TEXT,
    `createdAt` INTEGER NOT NULL
);

-- -----------------------------------------------------------------
-- 3. BẢNG TỪ VỰNG (vocabularies)
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `vocabularies` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `collectionId` INTEGER NOT NULL,
    `term` TEXT NOT NULL,
    `meaning` TEXT NOT NULL,
    `example` TEXT,
    `isLearned` INTEGER NOT NULL DEFAULT 0,
    `createdAt` INTEGER NOT NULL,
    FOREIGN KEY(`collectionId`) REFERENCES `collections`(`id`) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS `index_vocabularies_collectionId` ON `vocabularies` (`collectionId`);

-- -----------------------------------------------------------------
-- 4. BẢNG LỊCH SỬ DỊCH THUẬT (history)
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `history` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `sourceText` TEXT NOT NULL,
    `translatedText` TEXT NOT NULL,
    `sourceLang` TEXT NOT NULL,
    `targetLang` TEXT NOT NULL,
    `isFavorite` INTEGER NOT NULL DEFAULT 0,
    `timestamp` INTEGER NOT NULL
);

-- -----------------------------------------------------------------
-- 5. BẢNG PHIÊN TRÒ CHUYỆN CHAT AI (chat_sessions)
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `chat_sessions` (
    `id` TEXT NOT NULL PRIMARY KEY,
    `title` TEXT NOT NULL DEFAULT 'Cuộc trò chuyện mới',
    `createdAt` INTEGER NOT NULL,
    `updatedAt` INTEGER NOT NULL,
    `messageCount` INTEGER NOT NULL DEFAULT 0
);

-- -----------------------------------------------------------------
-- 6. BẢNG TIN NHẮN TRÒ CHUYỆN CHAT AI (chat_history)
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `chat_history` (
    `id` TEXT NOT NULL PRIMARY KEY,
    `sessionId` TEXT NOT NULL,
    `text` TEXT NOT NULL,
    `isUser` INTEGER NOT NULL,
    `timestamp` INTEGER NOT NULL,
    `isError` INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY(`sessionId`) REFERENCES `chat_sessions`(`id`) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS `index_chat_history_sessionId` ON `chat_history` (`sessionId`);

-- =================================================================
-- DỮ LIỆU MẪU BAN ĐẦU (SAMPLE DATA INSERTS)
-- =================================================================

-- Thêm bộ sưu tập mẫu
INSERT INTO `collections` (`id`, `name`, `description`, `accentColor`, `createdAt`) VALUES
(1, 'Từ vựng Giao tiếp Hằng ngày', 'Các từ vựng thông dụng dùng trong giao tiếp', '#8EC5FC', 1725000000000),
(2, 'Từ vựng Học tập & AI', 'Từ vựng chuyên ngành công nghệ và AI', '#81C784', 1725000000000);

-- Thêm từ vựng mẫu
INSERT INTO `vocabularies` (`collectionId`, `term`, `meaning`, `example`, `isLearned`, `createdAt`) VALUES
(1, 'Accomplish', 'Hoàn thành, đạt được mục tiêu', 'We can accomplish this goal together.', 1, 1725000000000),
(1, 'Persistent', 'Kiên trì, bền bỉ', 'Practice makes perfect if you are persistent.', 0, 1725000000000),
(2, 'Artificial Intelligence', 'Trí tuệ nhân tạo', 'AI is transforming language learning.', 1, 1725000000000);

-- Thêm phiên chat mẫu
INSERT INTO `chat_sessions` (`id`, `title`, `createdAt`, `updatedAt`, `messageCount`) VALUES
('session_sample_01', 'Tìm hiểu thì hiện tại đơn', 1725000000000, 1725000000000, 2);

-- Thêm tin nhắn chat mẫu
INSERT INTO `chat_history` (`id`, `sessionId`, `text`, `isUser`, `timestamp`, `isError`) VALUES
('msg_01', 'session_sample_01', 'Hiện tại đơn dùng khi nào bạn?', 1, 1725000000000, 0),
('msg_02', 'session_sample_01', 'Thì hiện tại đơn (Present Simple) dùng để diễn tả chân lý, sự thật hiển nhiên hoặc thói quen hằng ngày.', 0, 1725000000100, 0);
