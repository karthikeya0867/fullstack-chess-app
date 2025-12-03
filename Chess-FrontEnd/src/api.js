import axios from 'axios';

const API_BASE_URL = "http://localhost:8080/api";

const axiosInstance = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request interceptor to add the auth token to headers
axiosInstance.interceptors.request.use(
    (config) => {
        const authToken = localStorage.getItem('authToken'); // Retrieve token from localStorage
        if (authToken) {
            config.headers.Authorization = `Bearer ${authToken}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export const auth = {
    register: (username, password) => axiosInstance.post('/auth/register', { username, password }),
    login: (username, password) => axiosInstance.post('/auth/login', { username, password }),
};

export const games = {
    createGame: () => axiosInstance.post('/games'),
    joinGame: (gameId) => axiosInstance.post(`/games/${gameId}/join`),
    getOpenGames: () => axiosInstance.get('/games/open'),
    getGameState: (gameId) => axiosInstance.get(`/games/${gameId}`), // Changed to /games/{gameId} from /games/{gameId}/board
    makeMove: (gameId, move) => axiosInstance.post(`/games/${gameId}/move`, move),
    promotePawn: (gameId, pieceType, position) => axiosInstance.post(`/games/${gameId}/promote`, { pieceType, position }),
    resetGame: (gameId) => axiosInstance.post(`/games/${gameId}/reset`),
};

// Also expose axiosInstance for direct use if needed (e.g., in useWebSocket if it needs auth headers)
export default axiosInstance;
