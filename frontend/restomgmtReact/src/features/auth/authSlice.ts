import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { authApi, type RegisterPayload } from '../../api/authApi';
import { decodeJwt } from '../../api/jwt';

interface AuthState {
    token: string | null;
    username: string | null;
    roles: string[];
    isLoggedIn: boolean;
    loginStatus: 'idle' | 'loading' | 'failed';
    loginError: string | null;
    registerStatus: 'idle' | 'loading' | 'succeeded' | 'failed';
    registerError: string | null;
}

const storedToken = localStorage.getItem('auth_token');
const decodedOnLoad = storedToken ? decodeJwt(storedToken) : null;

const initialState: AuthState = {
    token: storedToken,
    username: decodedOnLoad?.sub ?? null,
    roles: decodedOnLoad?.roles ?? [],
    isLoggedIn: !!storedToken,
    loginStatus: 'idle',
    loginError: null,
    registerStatus: 'idle',
    registerError: null,
};

export const loginUser = createAsyncThunk(
    'auth/loginUser',
    async ({ username, password }: { username: string; password: string }) => {
        const token = await authApi.login(username, password);
        return token;
    }
);

export const registerUser = createAsyncThunk(
    'auth/registerUser',
    async (payload: RegisterPayload) => {
        const message = await authApi.register(payload);
        return message;
    }
);

export const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        logout: (state) => {
            state.token = null;
            state.username = null;
            state.roles = [];
            state.isLoggedIn = false;
            localStorage.removeItem('auth_token');
        },
        clearRegisterStatus: (state) => {
            state.registerStatus = 'idle';
            state.registerError = null;
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(loginUser.pending, (state) => {
                state.loginStatus = 'loading';
                state.loginError = null;
            })
            .addCase(loginUser.fulfilled, (state, action) => {
                const decoded = decodeJwt(action.payload);
                state.token = action.payload;
                state.username = decoded?.sub ?? null;
                state.roles = decoded?.roles ?? [];
                state.isLoggedIn = true;
                state.loginStatus = 'idle';
                localStorage.setItem('auth_token', action.payload);
            })
            .addCase(loginUser.rejected, (state, action) => {
                state.loginStatus = 'failed';
                state.loginError = action.error.message ?? 'Login failed';
            })
            .addCase(registerUser.pending, (state) => {
                state.registerStatus = 'loading';
                state.registerError = null;
            })
            .addCase(registerUser.fulfilled, (state) => {
                state.registerStatus = 'succeeded';
            })
            .addCase(registerUser.rejected, (state, action) => {
                state.registerStatus = 'failed';
                state.registerError = action.error.message ?? 'Registration failed';
            });
    },
});

export const { logout, clearRegisterStatus } = authSlice.actions;
export default authSlice.reducer;