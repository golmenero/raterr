import { escapeHtml, posterUrl, getInitials, ratingStars } from './utils.js';

function renderResults(movies) {
    const container = document.getElementById('results');

    if (!Array.isArray(movies) || movies.length === 0) {
        container.innerHTML = '<div class="empty-state">Try with another title, year or original name.</div>';
        return;
    }

    container.innerHTML = movies.map((m) => {
        const poster = posterUrl(m.posterPath);
        const overview = m.overview && m.overview.trim().length > 0
            ? escapeHtml(m.overview)
            : 'No synopsis available right now.';
        const isRated = (m.ratingsCount || 0) > 0;

        const cardContent = `
            <article class="result-card ${isRated ? 'result-card-disabled' : ''}">
                <div class="poster-frame">
                    ${poster
                        ? `<img src="${poster}" alt="Poster of ${escapeHtml(m.title)}">`
                        : `<div class="poster-fallback">${escapeHtml(getInitials(m.title))}</div>`}
                </div>
                <div class="result-content">
                    <div class="tag-row result-meta-row">
                        <span class="tag">${m.releaseYear || 'Unknown year'}</span>
                        ${isRated ? `<span class="result-rating-inline">${ratingStars(m.averageScore)}</span>` : ''}
                    </div>
                    <h3 class="result-title">${escapeHtml(m.title)}</h3>
                    <p class="result-overview">${overview}</p>
                </div>
            </article>
        `;

        if (isRated) {
            return cardContent;
        }

        return `
            <a class="result-card-link" href="/rate?id=${m.tmdbId}" aria-label="Rate ${escapeHtml(m.title)}">
                ${cardContent}
            </a>
        `;
    }).join('');
}

async function search() {
    const q = document.getElementById('q').value.trim();
    if (!q) return;

    try {
        const res = await fetch('/api/search?q=' + encodeURIComponent(q));
        if (!res.ok) {
            document.getElementById('results').innerHTML = '<div class="empty-state">Could not complete the search.</div>';
            return;
        }

        const movies = await res.json();
        renderResults(movies);
    } catch (_) {
        document.getElementById('results').innerHTML = '<div class="empty-state">Connection lost while searching for results.</div>';
    }
}

document.getElementById('q').addEventListener('keypress', (e) => {
    if (e.key === 'Enter') search();
});

window.search = search;
