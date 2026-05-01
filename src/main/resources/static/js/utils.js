export function escapeHtml(value) {
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

export function posterUrl(path, size = 'w500') {
    return path ? `https://image.tmdb.org/t/p/${size}${path}` : '';
}

export function getInitials(title) {
    return (title || '?').trim().charAt(0).toUpperCase();
}

export function ratingStars(score, showNumber = false) {
    const safeScore = Number(score || 0);
    const filled = Math.max(0, Math.min(5, Math.round(safeScore / 2)));
    const stars = '★'.repeat(filled) + '☆'.repeat(5 - filled);
    
    if (showNumber) {
        return `<span class="stars" aria-hidden="true">${stars}</span><span>${safeScore.toFixed(2)}</span>`;
    }
    
    return `<span class="stars" aria-hidden="true">${stars}</span>`;
}

export function setMessage(elementId, text, type) {
    const box = document.getElementById(elementId);
    if (!box) return;
    box.textContent = text;
    box.className = `feedback ${type}`;
}
