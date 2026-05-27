// ==========================================================================
// FILE JAVASCRIPT ĐƠN GIẢN (DÀNH CHO HỌC TẬP)
// ==========================================================================

// 1. Tương tác với Ảnh đại diện (Profile Image)
// Khi click vào ảnh, đường viền của ảnh sẽ đổi màu ngẫu nhiên
const profileImg = document.querySelector('.profile-img');

profileImg.addEventListener('click', function() {
    // Danh sách các màu viền để chọn ngẫu nhiên
    const colors = ['#0066cc', '#28a745', '#dc3545', '#ffc107', '#17a2b8'];
    const randomColor = colors[Math.floor(Math.random() * colors.length)];
    
    // Thay đổi thuộc tính CSS border-color của ảnh đại diện
    profileImg.style.borderColor = randomColor;
    
    // In thông báo nhỏ ra Console của trình duyệt để kiểm thử
    console.log('Đã đổi màu viền ảnh thành: ' + randomColor);
});


// 2. Xử lý Form Hỏi & Đáp (Q&A Form)
// Khi submit form, lấy câu hỏi của người dùng hiển thị trực tiếp lên trang mà không tải lại trang
const qaForm = document.querySelector('#cau-hoi form');
const inputQuestion = document.querySelector('#cau-hoi input[type="text"]');
const qaSection = document.querySelector('#cau-hoi');

// Tạo một vùng hiển thị danh sách các câu hỏi đã đặt
const listTitle = document.createElement('h3'); // Tạo tiêu đề phụ <h3>
listTitle.textContent = "Các câu hỏi đã đặt:";
listTitle.style.marginTop = "20px";

const questionList = document.createElement('ul'); // Tạo thẻ danh sách <ul>

// Thêm tiêu đề và danh sách vào cuối khu vực Q&A
qaSection.appendChild(listTitle);
qaSection.appendChild(questionList);

qaForm.addEventListener('submit', function(event) {
    // Ngăn chặn hành vi mặc định của form (không load lại trang web)
    event.preventDefault();
    
    // Lấy giá trị trong ô nhập và loại bỏ khoảng trắng ở hai đầu
    const questionText = inputQuestion.value.trim();
    
    // Kiểm tra nếu ô nhập trống
    if (questionText === "") {
        alert("Vui lòng nhập câu hỏi trước khi gửi!");
        return;
    }
    
    // Tạo một thẻ danh sách <li> mới
    const newListItem = document.createElement('li');
    newListItem.textContent = questionText;
    
    // Thêm một chút CSS inline đơn giản bằng JS
    newListItem.style.color = '#0066cc';
    newListItem.style.fontWeight = 'bold';
    newListItem.style.padding = '5px 0';
    
    // Thêm phần tử <li> mới vào thẻ <ul> danh sách câu hỏi
    questionList.appendChild(newListItem);
    
    // Xóa trống ô nhập để sẵn sàng cho câu hỏi tiếp theo
    inputQuestion.value = "";
});
