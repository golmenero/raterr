import { escapeHtml, posterUrl, setMessage } from './utils.js';

const params = new URLSearchParams(window.location.search);
const id = params.get('id');
const sliderIds = ['directing', 'cinematography', 'acting', 'soundtrack', 'screenplay'];

function updateSliderValue(inputId) {
    const input = document.getElementById(inputId);
    const value = document.getElementById(inputId + 'Value');
    value.textContent = parseFloat(input.value).toFixed(2);
}

function setInputsDisabled(disabled) {
    sliderIds.forEach((inputId) => {
        document.getElementById(inputId).disabled = disabled;
    });
    document.getElementById('btnSave').disabled = disabled;
}

function renderMovieSummary(movie) {
    const container = document.getElementById('movieSummary');
    const poster = posterUrl(movie.posterPath, 'w185');
    const overview = movie.overview && movie.overview.trim().length > 0
        ? escapeHtml(movie.overview)
        : 'No synopsis available for this title.';

    container.innerHTML = `
        <div class="poster-frame movie-inline-poster">
            ${poster
                ? `<img src="${poster}" alt="Poster of ${escapeHtml(movie.title)}">`
                : `<div class="poster-fallback">${escapeHtml((movie.title || '?').charAt(0).toUpperCase())}</div>`}
        </div>
        <div class="movie-inline-body">
            <div class="tag-row">
                <span class="tag">${movie.releaseYear || 'Unknown year'}</span>
                <span class="tag">TMDB: ${movie.tmdbVoteAverage || '—'}</span>
            </div>
            <p class="movie-inline-overview muted">${overview}</p>
        </div>
    `;
}

async function loadMovie() {
    try {
        const res = await fetch('/api/movie/' + id);
        if (!res.ok) {
            setMessage('msg', 'We could not find that movie.', 'error');
            setInputsDisabled(true);
            return;
        }

        const m = await res.json();
        document.getElementById('title').textContent = m.title;
        renderMovieSummary(m);

        if ((m.ratingsCount || 0) > 0) {
            setInputsDisabled(true);
            setMessage('msg', 'This movie already has a rating. If you want to change it, delete it first from Tops.', 'info');
        } else {
            setInputsDisabled(false);
            document.getElementById('msg').className = 'feedback hidden';
        }
    } catch (_) {
        setMessage('msg', 'Could not load the movie due to a network error.', 'error');
        setInputsDisabled(true);
    }
}

async function save() {
    const payload = {
        tmdbId: parseInt(id),
        directing: parseFloat(document.getElementById('directing').value),
        cinematography: parseFloat(document.getElementById('cinematography').value),
        acting: parseFloat(document.getElementById('acting').value),
        soundtrack: parseFloat(document.getElementById('soundtrack').value),
        screenplay: parseFloat(document.getElementById('screenplay').value)
    };

    try {
        const res = await fetch('/api/rate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            const data = await res.json();
            setMessage('msg', `Rating saved. New average: ${data.averageScore.toFixed(2)}`, 'success');
            setInputsDisabled(true);
            window.location.href = '/top';
        } else if (res.status === 409) {
            const err = await res.json();
            setMessage('msg', err.message || 'A rating already exists for this movie.', 'info');
            setInputsDisabled(true);
        } else {
            setMessage('msg', 'Could not save the rating.', 'error');
        }
    } catch (_) {
        setMessage('msg', 'A network error occurred while saving.', 'error');
    }
}

sliderIds.forEach((inputId) => {
    document.getElementById(inputId).addEventListener('input', () => updateSliderValue(inputId));
    updateSliderValue(inputId);
});

if (!id) {
    setMessage('msg', 'Missing movie ID.', 'error');
    setInputsDisabled(true);
} else {
    loadMovie();
}

window.save = save;
