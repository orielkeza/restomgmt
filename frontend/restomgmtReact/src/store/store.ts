import { configureStore } from '@reduxjs/toolkit';
import authReducer from '../features/auth/authSlice';

//this helps Redux work becuse it lets your global store know the slices exist

export const store = configureStore({
    reducer: {
        auth: authReducer,
    },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;