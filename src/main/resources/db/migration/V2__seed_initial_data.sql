-- ============================================================
-- SmartOffice — V2 초기 데이터 시드
-- 팀: 그로운 | 생성일: 2026-04-16
-- ============================================================
-- 초기 관리자 계정 및 기본 부서 데이터
-- 관리자 초기 비밀번호: EMP001 (BCrypt 해시)
-- 로그인 후 /api/v1/users/me 로 비밀번호 변경 권장
-- ============================================================

-- 기본 부서 등록
INSERT INTO departments (dept_name, dept_description, created_at, updated_at)
VALUES
    ('개발팀',   '소프트웨어 개발 담당',   NOW(), NOW()),
    ('IoT팀',    'IoT 하드웨어/펌웨어 담당', NOW(), NOW()),
    ('기획팀',   '서비스 기획 및 관리',     NOW(), NOW()),
    ('경영지원', '인사·회계·총무 담당',     NOW(), NOW());

-- 초기 관리자 계정 (EMP001 / 비밀번호: EMP001)
-- BCrypt(cost=10) 해시값 — BCryptPasswordEncoder.encode("EMP001")
INSERT INTO users (dept_id, employee_number, employee_name, employee_email,
                   password, role, position, phone, status, hired_at, created_at, updated_at)
VALUES (
    (SELECT dept_id FROM departments WHERE dept_name = '개발팀'),
    'EMP001',
    '관리자',
    'admin@grown.com',
    '$2a$10$G5RmabcW7DOESlmWETNJH.eO11./g2OS2/muEebMskHU1awZrRnra',
    'ADMIN',
    '팀장',
    '010-0000-0000',
    'ACTIVE',
    '2026-03-02',
    NOW(),
    NOW()
);
