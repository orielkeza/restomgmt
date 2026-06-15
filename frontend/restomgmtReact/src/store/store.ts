import { configureStore } from '@reduxjs/toolkit';
import authReducer from '../features/auth/authSlice';
import menuReducer from '../features/menu/menuSlice';
import dashboardReducer from '../features/dashboard/dashboardSlice';

//this helps Redux work becuse it lets your global store know the slices exist

export const store = configureStore({
    reducer: {
        auth: authReducer,
        menu: menuReducer,
        dashboard: dashboardReducer,
    },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;