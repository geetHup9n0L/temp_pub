Tôi đã tạo thêm file 
script_simple.js
 và kết nối trực tiếp vào tệp 
index_simple.html
 của bạn.

Các cơ chế JavaScript được thêm vào có cấu trúc rất đơn giản, dễ học và dễ phân tích như sau:

1. Click vào ảnh đại diện để đổi màu viền ngẫu nhiên (Thay đổi CSS trực tiếp)
Ý tưởng: Khi bạn nhấp chuột trái vào ảnh profile, đường viền ảnh sẽ ngẫu nhiên chuyển sang một trong các màu: Xanh dương, Xanh lá, Đỏ, Vàng, hoặc Xanh ngọc.
Kiến thức học tập:
document.querySelector('.profile-img'): Cách JS tìm và lấy phần tử ảnh từ HTML.
.addEventListener('click', ...): Lắng nghe sự kiện người dùng click vào phần tử.
profileImg.style.borderColor = randomColor: Cách JS can thiệp trực tiếp vào style (inline CSS) của một thẻ HTML.
2. Xử lý biểu mẫu Q&A không tải lại trang (Thao tác với DOM)
Ý tưởng: Khi bạn gõ câu hỏi vào ô nhập liệu và ấn nút Gửi (Submit):
Trang web sẽ không bị load lại (giống như ứng dụng thực tế Single Page Application).
JS tự động kiểm tra xem bạn đã nhập chữ chưa (nếu trống sẽ báo lỗi qua alert).
JS tự động tạo thêm một thẻ tiêu đề <h3>Các câu hỏi đã đặt:</h3> cùng một danh sách <ul> ở dưới cùng.
Mỗi câu hỏi mới gửi sẽ được tạo thành một thẻ <li> mới và tự động được đẩy (appendChild) vào danh sách dưới form.
Ô nhập liệu tự động xóa trống để bạn nhập câu hỏi tiếp theo.
Kiến thức học tập:
event.preventDefault(): Ngăn chặn hành vi tải lại trang mặc định của form HTML.
inputQuestion.value.trim(): Lấy nội dung người dùng nhập và cắt bỏ khoảng trắng thừa.
document.createElement('li'): Tạo một thẻ HTML mới hoàn toàn bằng JavaScript.
questionList.appendChild(newListItem): Gắn thẻ con vừa tạo vào bên trong thẻ cha trong cấu trúc DOM.
Hướng dẫn kiểm thử:
Bạn hãy mở file 
index_simple.html
 trên trình duyệt.
Thử nghiệm 1: Nhấp chuột liên tục vào ảnh đại diện để xem viền ảnh đổi màu.
Thử nghiệm 2: Nhập nội dung vào ô Đặt câu hỏi dưới góc phải và bấm nút Gửi. Bạn sẽ thấy câu hỏi xuất hiện ngay lập tức bên dưới form mà trang web không hề bị nhấp nháy/load lại.
Bạn có thể nhấn phím F12 trên bàn phím -> Chọn tab Console để xem các dòng thông báo được JS ghi lại trong quá trình bạn click đổi màu ảnh.
