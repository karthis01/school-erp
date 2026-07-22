function toCsvValue(value) {
  const str = value === null || value === undefined ? '' : String(value);
  if (/[",\n]/.test(str)) {
    return `"${str.replace(/"/g, '""')}"`;
  }
  return str;
}

/**
 * Downloads rows as a CSV file that opens directly in Excel.
 * @param {string} filename - without extension
 * @param {{ label: string, key: string }[]} columns
 * @param {object[]} rows
 */
export function exportToExcel(filename, columns, rows) {
  const header = columns.map((c) => toCsvValue(c.label)).join(',');
  const lines = rows.map((row) =>
    columns.map((c) => toCsvValue(typeof c.key === 'function' ? c.key(row) : row[c.key])).join(',')
  );
  const csv = [header, ...lines].join('\r\n');
  const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${filename}.csv`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}
