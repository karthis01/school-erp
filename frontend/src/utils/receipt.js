import api from '../api/axios';

// Strip the trailing /api so we can build a direct URL to static files like /uploads/logos/xyz.png
const API_ORIGIN = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api').replace(/\/api\/?$/, '');

// Builds a printable fee receipt in a new browser tab and opens the print dialog.
export async function printFeeReceipt(payment) {
  // Open the window synchronously (still inside the click's call stack) so
  // popup blockers don't kill it once we `await` the settings fetch below.
  const receiptWindow = window.open('', '_blank', 'width=700,height=850');
  if (!receiptWindow) {
    alert('Please allow pop-ups for this site to print the receipt.');
    return;
  }
  receiptWindow.document.write('<p style="font-family: sans-serif; padding: 40px;">Loading receipt…</p>');

  const student = payment.student || {};
  const structure = payment.feeStructure;

  let settings = {};
  try {
    const { data } = await api.get('/settings');
    settings = data || {};
  } catch (err) {
    settings = {};
  }
  const schoolName = settings.schoolName || 'Greenfield ERP';
  const tagline = settings.tagline || 'Academic Records Office';
  const logoSrc = settings.logoUrl ? `${API_ORIGIN}${settings.logoUrl}` : '';

  const receiptNo = `RCPT-${String(payment.id).padStart(5, '0')}`;
  const studentName = `${student.firstName || ''} ${student.lastName || ''}`.trim() || '—';
  const className = student.schoolClass ? `${student.schoolClass.className} - ${student.schoolClass.section}` : '—';

  const html = `
<!doctype html>
<html>
<head>
  <meta charset="utf-8" />
  <title>Receipt ${receiptNo}</title>
  <style>
    * { box-sizing: border-box; }
    body {
      font-family: Georgia, 'Times New Roman', serif;
      color: #21281F;
      padding: 40px;
      max-width: 640px;
      margin: 0 auto;
    }
    .header {
      text-align: center;
      border-bottom: 2px solid #1F4B43;
      padding-bottom: 16px;
      margin-bottom: 24px;
    }
    .header .logo {
      width: 64px;
      height: 64px;
      object-fit: contain;
      margin: 0 auto 8px;
      display: block;
    }
    .header h1 {
      margin: 0;
      font-size: 22px;
      color: #163831;
    }
    .header .sub {
      font-size: 12px;
      letter-spacing: 1px;
      text-transform: uppercase;
      color: #5C6157;
      margin-top: 4px;
    }
    .meta {
      display: flex;
      justify-content: space-between;
      font-size: 13px;
      margin-bottom: 20px;
      color: #5C6157;
    }
    table {
      width: 100%;
      border-collapse: collapse;
      margin-bottom: 24px;
      font-size: 14px;
    }
    td {
      padding: 8px 4px;
      border-bottom: 1px solid #E4E1D6;
    }
    td.label { color: #5C6157; width: 40%; }
    td.value { font-weight: 600; text-align: right; }
    .amount-row td {
      font-size: 18px;
      font-weight: 700;
      color: #1F4B43;
      border-bottom: none;
      border-top: 2px solid #1F4B43;
      padding-top: 14px;
    }
    .footer {
      margin-top: 40px;
      display: flex;
      justify-content: space-between;
      font-size: 12px;
      color: #5C6157;
    }
    .stamp {
      display: inline-block;
      font-size: 10px;
      letter-spacing: 1px;
      text-transform: uppercase;
      border: 1px solid #D98E2C;
      color: #D98E2C;
      border-radius: 999px;
      padding: 3px 10px;
      margin-top: 6px;
    }
    @media print {
      body { padding: 0; }
    }
  </style>
</head>
<body>
  <div class="header">
    ${logoSrc ? `<img class="logo" src="${logoSrc}" alt="${schoolName} logo" />` : ''}
    <h1>${schoolName} — Fee Receipt</h1>
    <div class="sub">${tagline}</div>
  </div>

  <div class="meta">
    <div>Receipt No: <strong>${receiptNo}</strong></div>
    <div>Date: <strong>${payment.paymentDate || '—'}</strong></div>
  </div>

  <table>
    <tr><td class="label">Student Name</td><td class="value">${studentName}</td></tr>
    <tr><td class="label">Admission Number</td><td class="value">${student.admissionNumber || '—'}</td></tr>
    <tr><td class="label">Class</td><td class="value">${className}</td></tr>
    <tr><td class="label">Fee Type</td><td class="value">${structure ? structure.feeType : 'General Payment'}</td></tr>
    <tr><td class="label">Payment Mode</td><td class="value">${payment.paymentMode || '—'}</td></tr>
    <tr><td class="label">Transaction Reference</td><td class="value">${payment.transactionRef || '—'}</td></tr>
    <tr><td class="label">Status</td><td class="value">${payment.status || 'PAID'}</td></tr>
    <tr class="amount-row"><td>Amount Paid</td><td class="value">₹${Number(payment.amountPaid || 0).toLocaleString()}</td></tr>
  </table>

  <div class="footer">
    <div>
      This is a computer-generated receipt.
      <div class="stamp">Received</div>
    </div>
    <div style="text-align: right;">
      _________________________<br />
      Authorized Signature
    </div>
  </div>
</body>
</html>`;

  receiptWindow.document.open();
  receiptWindow.document.write(html);
  receiptWindow.document.close();
  receiptWindow.focus();
  // Give the new window a moment to render before invoking print
  setTimeout(() => receiptWindow.print(), 300);
}
