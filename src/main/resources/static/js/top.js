import { escapeHtml, posterUrl, ratingStars, setMessage } from './utils.js';

function renderListCard(movie, index) {
    const poster = posterUrl(movie.posterPath);
    const showPoster = index < 3 && Boolean(poster);
    return `
        <article class="top-card list-poster-card ${showPoster ? '' : 'no-poster'}">
            ${showPoster
                ? `<div class="top-poster poster-frame"><img src="${poster}" alt="Poster of ${escapeHtml(movie.title)}"></div>`
                : ''}
            <div class="top-info">
                <div class="tag-row top-tag-row">
                    <span class="tag">#${index + 1}</span>
                    <span class="tag">${movie.releaseYear || 'Unknown year'}</span>
                </div>
                <h3 class="top-title">${escapeHtml(movie.title)}</h3>
                <p class="rating-inline compact-stars">${ratingStars(movie.averageScore, true)}</p>
            </div>
            <div class="top-actions">
                <button class="top-delete-button" type="button" onclick="deleteRating(${movie.tmdbId}, '${encodeURIComponent(String(movie.title))}')" aria-label="Delete rating">
                    <span class="button-icon" aria-hidden="true">🗑</span>
                </button>
            </div>
        </article>
    `;
}

async function loadTops() {
    const year = document.getElementById('year').value;
    const list = document.getElementById('list');

    let url = '/api/tops';
    if (year) url += '?year=' + year;

    try {
        const res = await fetch(url);
        if (!res.ok) {
            list.innerHTML = '<div class="empty-state">Could not load the tops.</div>';
            return;
        }

        const tops = await res.json();

        if (!Array.isArray(tops) || tops.length === 0) {
            list.innerHTML = '<div class="empty-state">No rated movies yet for this filter.</div>';
        } else {
            list.innerHTML = tops.map((m, index) => renderListCard(m, index)).join('');
        }
    } catch (_) {
        list.innerHTML = '<div class="empty-state">Connection lost while loading the ranking.</div>';
    }
}

async function deleteRating(tmdbId, titleEncoded) {
    const title = decodeURIComponent(titleEncoded);
    const confirmacion = window.confirm('Delete the rating for "' + title + '"?');
    if (!confirmacion) return;

    try {
        const res = await fetch('/api/movie/' + tmdbId + '/rating', {
            method: 'DELETE'
        });

        if (!res.ok) {
            setMessage('msg', 'Could not delete the rating.', 'error');
            return;
        }

        setMessage('msg', 'Rating deleted successfully.', 'success');
        await loadTops();
    } catch (_) {
        setMessage('msg', 'Network error while trying to delete.', 'error');
    }
}

loadTops();

window.deleteRating = deleteRating;
