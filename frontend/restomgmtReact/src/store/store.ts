import { configureStore } from '@reduxjs/toolkit';
import authReducer from '../features/auth/authSlice';
import menuReducer from '../features/menu/menuSlice';
import dashboardReducer from '../features/dashboard/dashboardSlice';
import cartReducer from '../features/cart/cartSlice';
import orderReducer from '../features/order/orderSlice';
import paymentReducer from '../features/payments/paymentSlice';
import userReducer from '../features/users/userSlice'

//this helps Redux work becuse it lets your global store know the slices exist

export const store = configureStore({
    reducer: {
        auth: authReducer,
        menu: menuReducer,
        dashboard: dashboardReducer,
        cart: cartReducer,
        orders: orderReducer,
        payments: paymentReducer,
        users: userReducer,
    },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;