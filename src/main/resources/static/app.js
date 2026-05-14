const api = '';
const state = {
  token: localStorage.getItem('token'),
  user: JSON.parse(localStorage.getItem('user') || 'null'),
  categories: [],
  products: [],
  selectedCategory: '',
  latestOrder: JSON.parse(localStorage.getItem('latestOrder') || 'null'),
  authMode: 'login'
};
const $ = (id) => document.getElementById(id);
const money = (value) => 'Rs ' + Number(value || 0).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const escapeHtml = (value) => String(value || '').replace(/[&<>"']/g, (m) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[m]));
const fallbackImage = 'https://images.unsplash.com/photo-1472851294608-062f824d29cc?auto=format&fit=crop&w=900&q=80';
const imageFor = (product) => product.imageUrl || fallbackImage;
const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

async function request(path, options = {}) {
  const headers = { ...(options.headers || {}) };
  const protectedPath = path.startsWith('/api/cart') || path.startsWith('/api/orders') || path.startsWith('/api/payments') || path.startsWith('/api/admin');
  if (options.body && !headers['Content-Type']) headers['Content-Type'] = 'application/json';
  if (state.token && protectedPath) headers.Authorization = 'Bearer ' + state.token;
  const response = await fetch(api + path, { ...options, headers });
  if (!response.ok) {
    let message = 'Request failed';
    try { const error = await response.json(); message = (error.details && error.details[0]) || error.error || message; } catch (_) {}
    if (response.status === 401) logout(false);
    throw new Error(message);
  }
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

function toast(message) { const node = $('toast'); node.textContent = message; node.classList.add('show'); setTimeout(() => node.classList.remove('show'), 2600); }
function showError(error) { toast(error.message || 'Something went wrong'); }
function isStrongPassword(password) { return /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/.test(password || ''); }
function setPasswordHelp(valid) { const help = $('passwordHelp'); if (help) help.classList.toggle('invalid', !valid); }
function saveSession(auth) { state.token = auth.token; state.user = { id: auth.userId, name: auth.name, email: auth.email, role: auth.role }; localStorage.setItem('token', state.token); localStorage.setItem('user', JSON.stringify(state.user)); renderAuth(); }
function logout(showToast = true) { state.token = null; state.user = null; localStorage.removeItem('token'); localStorage.removeItem('user'); renderAuth(); renderCart({ items: [], total: 0 }); if (showToast) toast('Signed out'); }
function renderAuth() { $('authButton').textContent = state.user ? state.user.name + ' / Sign out' : 'Sign in'; $('adminTab').style.display = state.user && state.user.role === 'ADMIN' ? 'inline-flex' : 'none'; }
function setView(view) { $('shopHero').classList.toggle('hidden', view !== 'shop'); $('shopView').classList.toggle('hidden', view !== 'shop'); $('adminView').classList.toggle('hidden', view !== 'admin'); $('catalogTab').classList.toggle('active', view === 'shop'); $('adminTab').classList.toggle('active', view === 'admin'); if (view === 'admin') loadAdmin().catch(showError); }

async function loadCatalog() {
  state.categories = await request('/api/categories');
  state.products = await request('/api/products' + (state.selectedCategory ? '?category=' + encodeURIComponent(state.selectedCategory) : ''));
  $('heroProductCount').textContent = state.products.length;
  renderCategories();
  renderProducts(state.products);
  fillCategorySelect();
}
function renderCategories() {
  const list = $('categoryList');
  list.innerHTML = '';
  const all = document.createElement('button');
  all.textContent = 'All products';
  all.className = state.selectedCategory ? '' : 'active';
  all.onclick = () => { state.selectedCategory = ''; loadCatalog().catch(showError); };
  list.appendChild(all);
  state.categories.forEach((category) => {
    const btn = document.createElement('button');
    btn.textContent = category.name;
    btn.className = state.selectedCategory === category.slug ? 'active' : '';
    btn.onclick = () => { state.selectedCategory = category.slug; loadCatalog().catch(showError); };
    list.appendChild(btn);
  });
}
function renderProducts(products) {
  $('catalogSummary').textContent = products.length + ' active product' + (products.length === 1 ? '' : 's');
  const grid = $('productGrid');
  if (!products.length) { grid.innerHTML = '<div class="empty-state">No products found</div>'; return; }
  grid.innerHTML = products.map((p) => '<article class="product-card"><div class="product-image" style="background-image:url(' + imageFor(p) + ')"><span class="stock-pill">' + p.stockQuantity + ' in stock</span></div><div class="product-body"><div><h3>' + escapeHtml(p.name) + '</h3><p class="product-meta">' + escapeHtml(p.description || p.category.name) + '</p></div><div class="price-row"><span class="price">' + money(p.price) + '</span><button class="primary" data-add="' + p.id + '">Add</button></div></div></article>').join('');
  grid.querySelectorAll('[data-add]').forEach((button) => button.onclick = () => addToCart(button.dataset.add).catch(showError));
}
async function addToCart(productId) { if (!state.token) return openAuth(); const cart = await request('/api/cart/items', { method: 'POST', body: JSON.stringify({ productId: Number(productId), quantity: 1 }) }); renderCart(cart); await loadCatalog(); toast('Added to cart and stock reserved'); }
async function loadCart() { if (!state.token) return renderCart({ items: [], total: 0 }); renderCart(await request('/api/cart')); }
function renderCart(cart) {
  $('cartCount').textContent = cart.items.reduce((sum, item) => sum + item.quantity, 0);
  $('cartTotal').textContent = money(cart.total);
  $('cartItems').innerHTML = cart.items.length ? cart.items.map((item) => '<div class="cart-row"><div class="row-between"><strong>' + escapeHtml(item.productName) + '</strong><span>' + money(item.subtotal) + '</span></div><div class="row-between"><span>' + money(item.unitPrice) + ' x ' + item.quantity + '</span><button class="secondary" data-remove="' + item.id + '">Remove</button></div></div>').join('') : '<div class="empty-state">Your cart is empty</div>';
  $('cartItems').querySelectorAll('[data-remove]').forEach((button) => button.onclick = async () => { await request('/api/cart/items/' + button.dataset.remove, { method: 'DELETE' }); await loadCart(); await loadCatalog(); toast('Removed and stock restored'); });
}
async function checkout(event) {
  event.preventDefault();
  if (!state.token) return openAuth();
  const shippingAddress = $('shippingAddress').value.trim();
  const order = await request('/api/orders/checkout', { method: 'POST', body: JSON.stringify({ shippingAddress }) });
  state.latestOrder = order;
  localStorage.setItem('latestOrder', JSON.stringify(order));
  $('shippingAddress').value = '';
  await loadCart();
  await loadCatalog();
  openPayment(order);
}
function openPayment(order) {
  state.latestOrder = order || state.latestOrder;
  if (!state.latestOrder) return toast('Checkout first, then pay');
  $('paymentOrderId').textContent = '#' + state.latestOrder.id;
  $('paymentAmount').textContent = money(state.latestOrder.totalAmount);
  $('paymentName').value = state.user ? state.user.name : '';
  resetPaymentUi();
  $('paymentModal').classList.remove('hidden');
}
function closePayment() { $('paymentModal').classList.add('hidden'); }
function selectedPaymentMethod() { return document.querySelector('input[name="payMethod"]:checked').value; }
function setPaymentStep(step) {
  $('payStepDetails').classList.toggle('active', step === 'details');
  $('payStepVerify').classList.toggle('active', step === 'verify');
  $('payStepDone').classList.toggle('active', step === 'done');
}
function resetPaymentUi() {
  setPaymentStep('details');
  $('paymentStatus').textContent = 'Enter dummy details. No real money is charged.';
  $('paymentReceipt').classList.add('hidden');
  $('paymentReceipt').innerHTML = '';
  $('paymentOtp').value = '';
  $('completePaymentButton').disabled = false;
  $('completePaymentButton').textContent = 'Pay Securely';
  updatePaymentMethod();
}
function updatePaymentMethod() {
  const isUpi = selectedPaymentMethod() === 'RAZORPAY';
  $('upiFields').classList.toggle('hidden', !isUpi);
  $('cardFields').classList.toggle('hidden', isUpi);
  $('paymentStatus').textContent = isUpi ? 'Use any demo UPI ID and OTP 123456.' : 'Use any 16 digit demo card, future expiry, CVV, and OTP 123456.';
}
function validatePaymentDetails(provider) {
  if (!$('paymentName').value.trim()) return 'Enter the name on payment';
  if (provider === 'RAZORPAY' && !/^[\w.-]+@[\w.-]+$/.test($('paymentUpi').value.trim())) return 'Enter a valid demo UPI ID like demo@upi';
  if (provider === 'MOCK') {
    const card = $('paymentCard').value.replace(/\s/g, '');
    if (!/^\d{16}$/.test(card)) return 'Enter a 16 digit demo card number';
    if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test($('paymentExpiry').value.trim())) return 'Enter expiry as MM/YY';
    if (!/^\d{3}$/.test($('paymentCvv').value.trim())) return 'Enter a 3 digit CVV';
  }
  if ($('paymentOtp').value.trim() !== '123456') return 'Enter demo OTP 123456';
  return '';
}
function sendDemoOtp() {
  setPaymentStep('verify');
  $('paymentOtp').value = '123456';
  $('paymentStatus').textContent = 'Demo OTP generated: 123456';
  toast('Demo OTP is 123456');
}
async function completePayment() {
  if (!state.latestOrder) return toast('Checkout first, then pay');
  const provider = selectedPaymentMethod();
  const validation = validatePaymentDetails(provider);
  if (validation) { setPaymentStep('details'); return toast(validation); }
  setPaymentStep('verify');
  $('completePaymentButton').disabled = true;
  $('completePaymentButton').textContent = 'Processing...';
  $('paymentStatus').textContent = 'Contacting dummy bank and verifying payment...';
  await delay(900);
  $('paymentStatus').textContent = 'Payment authorized. Saving transaction...';
  try {
    const payment = await request('/api/payments', { method: 'POST', body: JSON.stringify({ orderId: Number(state.latestOrder.id), provider }) });
    setPaymentStep('done');
    $('paymentStatus').textContent = 'Payment successful. Demo receipt generated.';
    $('paymentReceipt').classList.remove('hidden');
    $('paymentReceipt').innerHTML = '<strong>Payment complete</strong><span>Transaction ID</span><code>' + escapeHtml(payment.transactionId) + '</code><span>Status</span><b>' + payment.status + ' • ' + money(payment.amount) + '</b>';
    $('completePaymentButton').textContent = 'Paid';
    toast('Payment success: ' + payment.transactionId.slice(0, 18));
    if (state.user && state.user.role === 'ADMIN') await loadAdmin();
  } catch (error) {
    $('completePaymentButton').disabled = false;
    $('completePaymentButton').textContent = 'Pay Securely';
    $('paymentStatus').textContent = 'Payment could not be completed. Check the order and try again.';
    throw error;
  }
}

function openAuth() { $('authModal').classList.remove('hidden'); }
function closeAuth() { $('authModal').classList.add('hidden'); }
function setAuthMode(mode) {
  state.authMode = mode;
  const register = mode === 'register';
  $('nameField').classList.toggle('hidden', !register);
  $('verificationTools').classList.toggle('hidden', !register);
  $('verificationHelp').classList.toggle('hidden', !register);
  if ($('passwordHelp')) $('passwordHelp').classList.toggle('hidden', !register);
  if ($('passwordField')) $('passwordField').placeholder = register ? 'Strong password' : 'Password';
  if (!register) $('verificationCodeField').value = '';
  setPasswordHelp(true);
  $('authTitle').textContent = register ? 'Create account' : 'Sign in';
  $('authSubmit').textContent = register ? 'Create account' : 'Sign in';
  $('toggleAuth').textContent = register ? 'I already have an account' : 'Create a new account';
}
async function sendVerificationCode() {
  const email = $('authForm').elements.email.value.trim();
  if (!email) return toast('Enter your email first');
  $('sendVerificationButton').disabled = true;
  $('sendVerificationButton').textContent = 'Sending...';
  try {
    const result = await request('/api/auth/verification-code', { method: 'POST', body: JSON.stringify({ email }) });
    $('verificationCodeField').value = result.demoCode;
    $('verificationHelp').textContent = result.message + '. Demo code: ' + result.demoCode;
    toast('Verification code sent');
  } finally {
    $('sendVerificationButton').disabled = false;
    $('sendVerificationButton').textContent = 'Send Code';
  }
}
async function submitAuth(event) {
  event.preventDefault();
  const payload = Object.fromEntries(new FormData(event.currentTarget).entries());
  if (state.authMode === 'login') {
    delete payload.name;
    delete payload.verificationCode;
  } else if (!payload.verificationCode || payload.verificationCode.trim().length !== 6) {
    toast('Enter the 6 digit email verification code');
    return;
  } else if (!isStrongPassword(payload.password)) {
    setPasswordHelp(false);
    toast('Please create a stronger password');
    return;
  }
  const auth = await request('/api/auth/' + (state.authMode === 'register' ? 'register' : 'login'), { method: 'POST', body: JSON.stringify(payload) });
  saveSession(auth);
  closeAuth();
  await loadCart();
  toast('Welcome, ' + auth.name);
}
function fillCategorySelect() { $('productCategorySelect').innerHTML = state.categories.map((c) => '<option value="' + c.id + '">' + escapeHtml(c.name) + '</option>').join(''); }
function updatePreview() {
  const value = $('productImageUrl').value.trim();
  const img = $('imagePreviewImg');
  const label = $('imagePreview').querySelector('span');
  if (value) {
    img.src = value;
    img.classList.remove('hidden');
    label.textContent = 'Preview loaded';
  } else {
    img.removeAttribute('src');
    img.classList.add('hidden');
    label.textContent = 'Paste a product photo URL';
  }
}

async function loadAdmin() {
  if (!state.user || state.user.role !== 'ADMIN') return toast('Admin login required');
  const result = await Promise.all([request('/api/admin/dashboard'), request('/api/admin/orders'), request('/api/products')]);
  const dashboard = result[0];
  const orders = result[1];
  const products = result[2];
  $('metrics').innerHTML = [['Users', dashboard.users], ['Products', products.length], ['Orders', dashboard.orders], ['Payments', dashboard.payments]].map(([label, value]) => '<div class="metric"><span>' + label + '</span><strong>' + value + '</strong></div>').join('');
  renderAdminProducts(products);
  renderAdminOrders(orders);
}
function renderAdminProducts(products) {
  $('adminProducts').innerHTML = products.length ? products.map((p) => '<article class="admin-product"><img src="' + imageFor(p) + '" alt=""><div class="admin-product-main"><div class="admin-product-copy"><h4>' + escapeHtml(p.name) + '</h4><p>' + escapeHtml(p.sku) + ' • ' + money(p.price) + '</p></div><div class="stock-line"><span>Current stock</span><strong>' + p.stockQuantity + '</strong></div><div class="admin-actions"><input type="number" min="1" value="1" data-count="' + p.id + '" aria-label="Inventory count"><button class="secondary" data-restock="' + p.id + '">Add</button><button class="secondary" data-reduce="' + p.id + '">Lower</button><button class="danger" data-delete="' + p.id + '">Delete</button></div></div></article>').join('') : '<div class="empty-state">No active products</div>';
  $('adminProducts').querySelectorAll('[data-restock]').forEach((button) => button.onclick = () => adjustProductStock(button.dataset.restock, 1).catch(showError));
  $('adminProducts').querySelectorAll('[data-reduce]').forEach((button) => button.onclick = () => adjustProductStock(button.dataset.reduce, -1).catch(showError));
  $('adminProducts').querySelectorAll('[data-delete]').forEach((button) => button.onclick = () => deleteProduct(button.dataset.delete).catch(showError));
}
async function adjustProductStock(productId, direction) { const input = document.querySelector('[data-count="' + productId + '"]'); const rawCount = Number(input.value || 0); if (rawCount <= 0) return toast('Enter a count above 0'); const quantityChange = rawCount * direction; await request('/api/admin/products/' + productId + '/inventory', { method: 'PATCH', body: JSON.stringify({ quantityChange, reason: direction > 0 ? 'Admin restock' : 'Admin stock reduction' }) }); await loadCatalog(); await loadAdmin(); toast(direction > 0 ? 'Stock increased' : 'Stock lowered'); }
async function deleteProduct(productId) { await request('/api/admin/products/' + productId, { method: 'DELETE' }); await loadCatalog(); await loadAdmin(); toast('Product removed from store'); }
function renderAdminOrders(orders) {
  $('adminOrders').innerHTML = orders.length ? orders.map((order) => {
    const items = order.items.map((item) => '<div>' + escapeHtml(item.productName) + ' x ' + item.quantity + ' - ' + money(item.subtotal) + '</div>').join('');
    return '<article class="order-row"><div class="order-title"><div><strong>Order #' + order.id + '</strong><div class="order-customer">' + escapeHtml(order.customerName) + ' • ' + escapeHtml(order.customerEmail) + '<br>' + escapeHtml(order.shippingAddress) + '</div></div><select data-order="' + order.id + '">' + ['PENDING','PAID','SHIPPED','DELIVERED','CANCELLED'].map((s) => '<option ' + (s === order.status ? 'selected' : '') + '>' + s + '</option>').join('') + '</select></div><div class="order-items">' + items + '</div><div class="row-between"><span>' + new Date(order.createdAt).toLocaleString() + '</span><strong>' + money(order.totalAmount) + '</strong></div></article>';
  }).join('') : '<div class="empty-state">No orders yet</div>';
  $('adminOrders').querySelectorAll('[data-order]').forEach((select) => select.onchange = async () => { await request('/api/admin/orders/' + select.dataset.order + '/status', { method: 'PATCH', body: JSON.stringify({ status: select.value }) }); toast('Order updated'); });
}
async function createCategory(event) { event.preventDefault(); const payload = Object.fromEntries(new FormData(event.currentTarget).entries()); await request('/api/admin/categories', { method: 'POST', body: JSON.stringify(payload) }); event.currentTarget.reset(); await loadCatalog(); await loadAdmin(); toast('Category created'); }
async function createProduct(event) { event.preventDefault(); const payload = Object.fromEntries(new FormData(event.currentTarget).entries()); payload.price = Number(payload.price); payload.stockQuantity = Number(payload.stockQuantity); payload.categoryId = Number(payload.categoryId); payload.active = true; await request('/api/admin/products', { method: 'POST', body: JSON.stringify(payload) }); event.currentTarget.reset(); updatePreview(); await loadCatalog(); await loadAdmin(); toast('Product created'); }

$('catalogTab').onclick = () => setView('shop');
$('adminTab').onclick = () => setView('admin');
$('refreshButton').onclick = () => loadCatalog().catch(showError);
$('reloadAdminButton').onclick = () => loadAdmin().catch(showError);
$('cartButton').onclick = () => { $('cartDrawer').classList.add('open'); loadCart().catch(showError); };
$('closeCart').onclick = () => $('cartDrawer').classList.remove('open');
$('authButton').onclick = () => state.user ? logout() : openAuth();
$('closeAuth').onclick = closeAuth;
$('toggleAuth').onclick = () => setAuthMode(state.authMode === 'login' ? 'register' : 'login');
$('authForm').onsubmit = (event) => submitAuth(event).catch(showError);
$('sendVerificationButton').onclick = () => sendVerificationCode().catch(showError);
$('checkoutForm').onsubmit = (event) => checkout(event).catch(showError);
$('closePayment').onclick = closePayment;
$('sendOtpButton').onclick = sendDemoOtp;
document.querySelectorAll('input[name="payMethod"]').forEach((input) => input.onchange = updatePaymentMethod);
$('completePaymentButton').onclick = () => completePayment().catch(showError);
$('categoryForm').onsubmit = (event) => createCategory(event).catch(showError);
$('productForm').onsubmit = (event) => createProduct(event).catch(showError);
$('productImageUrl').oninput = updatePreview;
updatePreview();
$('searchInput').oninput = async (event) => { const q = event.target.value.trim(); renderProducts(q ? await request('/api/products/search?q=' + encodeURIComponent(q)) : state.products); };
if ($('passwordField')) $('passwordField').oninput = () => { if (state.authMode === 'register') setPasswordHelp(isStrongPassword($('passwordField').value)); };
document.querySelectorAll('.demo-users button').forEach((button) => button.onclick = async () => { const auth = await request('/api/auth/login', { method: 'POST', body: JSON.stringify({ email: button.dataset.email, password: button.dataset.password, role: button.dataset.role }) }); saveSession(auth); closeAuth(); await loadCart(); toast('Signed in as ' + auth.role.toLowerCase()); });

renderAuth();
setAuthMode('login');
setView('shop');
loadCatalog().then(loadCart).catch(showError);
