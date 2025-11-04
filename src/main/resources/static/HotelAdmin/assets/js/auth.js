// assets/js/auth.js

/**
 * ===================================================================
 * FILE: auth.js
 * MÔ TẢ: Script chung cho toàn bộ trang admin.
 * CHỨC NĂNG: Xác thực, hiển thị thông tin profile, và xử lý đăng xuất.
 * ===================================================================
 */

// Biến global theo dõi trạng thái chuyển hướng
let isRedirectingForAuth = false;

/**
 * Hàm fetch() đã được "bọc" thêm logic chèn JWT Token
 * (ĐÃ SỬA LỖI: Tự động xử lý Content-Type cho FormData)
 * @param {string} url - Đường dẫn API
 * @param {object} options - Cấu hình của fetch (method, body, v.v.)
 * @returns {Promise<Response>}
 */
async function fetchWithAuth(url, options = {}) {
    const token = localStorage.getItem('jwtToken');
    const headers = new Headers(options.headers || {});

    // === PHẦN SỬA LỖI QUAN TRỌNG ===
    // Chỉ set Content-Type: json NẾU body là object VÀ KHÔNG phải FormData
    // Nếu body là FormData, trình duyệt sẽ tự động set Content-Type đúng
    if (options.body && !(options.body instanceof FormData) && typeof options.body === 'object') {
        if (!headers.has('Content-Type')) {
            headers.set('Content-Type', 'application/json');
        }
        // Chuyển object thành JSON string
        options.body = JSON.stringify(options.body);
    }
    // === KẾT THÚC PHẦN SỬA LỖI ===

    if (token) {
        headers.set('Authorization', `Bearer ${token}`);
    } else {
        console.warn(`[AUTH] Không tìm thấy token cho yêu cầu tới ${url}.`);
        if (!isRedirectingForAuth && !window.location.pathname.includes('pages-login.html')) {
            isRedirectingForAuth = true;
            alert('Không tìm thấy thông tin đăng nhập. Đang chuyển hướng...');
            window.location.href = '/HotelAdmin/pages-login.html'; // Sửa lại đường dẫn cho đúng
            throw new Error('Unauthorized');
        }
    }

    const finalOptions = { ...options, headers };

    try {
        const response = await fetch(url, finalOptions);

        if (response.status === 401 && !isRedirectingForAuth) {
            isRedirectingForAuth = true;
            console.error(`[AUTH] Lỗi 401 Unauthorized cho ${url}.`);
            alert('Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.');
            localStorage.removeItem('jwtToken');
            localStorage.removeItem('currentUser'); // Xóa cả thông tin user
            window.location.href = '/HotelAdmin/pages-login.html';
            throw new Error('Unauthorized');
        }

        if (!response.ok && response.status !== 401) {
            console.error(`[AUTH] Lỗi HTTP ${response.status} cho ${url}`);
            let errorBody = `Lỗi ${response.status}`;
            try {
                // Thử đọc lỗi dạng JSON (Spring Boot hay trả về)
                const errorJson = await response.json();
                errorBody = errorJson.message || errorJson.error || JSON.stringify(errorJson);
            } catch (e) {
                try {
                    // Nếu không phải JSON, thử đọc dạng text
                    errorBody = await response.text();
                } catch (e2) {}
            }
            throw new Error(errorBody); // Ném lỗi (ví dụ: "Lỗi: Mật khẩu hiện tại không chính xác!")
        }
        return response;
    } catch (error) {
        if (error.name !== 'AbortError' && error.message !== 'Unauthorized') {
            console.error(`[AUTH] Lỗi fetch/mạng cho ${url}:`, error);
        }
        throw error; // Ném lỗi ra ngoài để hàm gọi .catch()
    }
}


/**
 * Tải thông tin user từ localStorage và cập nhật header
 */
function loadProfileFromStorage() {
    try {
        const userString = localStorage.getItem('currentUser');
        if (!userString) {
            console.warn('Không tìm thấy "currentUser" trong localStorage...');
            // (Giữ nguyên logic throw new Error...)
            throw new Error('Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.');
        }

        const user = JSON.parse(userString);

        // --- Lấy các phần tử HTML ---
        const profileName = document.getElementById('profileName');
        const profileDropdownName = document.getElementById('profileDropdownName');
        const profileDropdownRole = document.getElementById('profileDropdownRole');

        // --- BỔ SUNG: Lấy phần tử ảnh ---
        // Tìm tất cả ảnh profile trên header (có alt="Hồ sơ")
        const profileImages = document.querySelectorAll('.header-nav img[alt="Hồ sơ"]');

        // --- Cập nhật Text ---
        if (profileName) profileName.textContent = user.fullName || "Người dùng";
        if (profileDropdownName) profileDropdownName.textContent = user.fullName || "Người dùng";
        if (profileDropdownRole) profileDropdownRole.textContent = user.role || "Chưa có vai trò";

        // --- BỔ SUNG: Cập nhật Ảnh ---
        if (profileImages.length > 0) {
            let avatarSrc = 'assets/img/profile-img.jpg'; // Ảnh mặc định
            if (user.avatarUrl) {
                // Đường dẫn ảnh (đã cấu hình trong MvcConfig.java)
                avatarSrc = `/avatars/${user.avatarUrl}`;
            }

            profileImages.forEach(img => {
                img.src = avatarSrc;
            });
        }

    } catch (error) {
        // (Giữ nguyên khối catch error)
        console.error("Lỗi khi tải hồ sơ từ localStorage:", error);
        localStorage.removeItem('jwtToken');
        localStorage.removeItem('currentUser');
        if (!window.location.pathname.includes('pages-login.html')) {
            window.location.href = '/HotelAdmin/pages-login.html';
        }
    }
}
/**
 * Gắn sự kiện cho nút Đăng xuất
 */
function setupLogoutButton() {
    const logoutBtn = document.getElementById('logoutButton');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            localStorage.removeItem('jwtToken');
            localStorage.removeItem('currentUser');
            alert('Bạn đã đăng xuất.');
            window.location.href = '/HotelAdmin/pages-login.html';
        });
    }
}

// === KHỞI CHẠY CÁC HÀM CHUNG ===
document.addEventListener('DOMContentLoaded', () => {
    // Không chạy các hàm này trên trang login
    if (!window.location.pathname.includes('pages-login.html')) {
        loadProfileFromStorage();
        setupLogoutButton();
    }
});