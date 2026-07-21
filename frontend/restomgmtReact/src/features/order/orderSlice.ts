import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { orderApi, type OrderResponse, type OrderStatus } from '../../api/orderApi';
import type { RootState } from '../../store/store';

interface OrderState {
    myOrders: OrderResponse[];
    allOrders: OrderResponse[];
    status: 'idle' | 'loading' | 'succeeded' | 'failed';
    mutationStatus: 'idle' | 'loading' | 'failed';
    error: string | null;
    lastPlacedOrder: OrderResponse | null;
    pendingOrderIds: number[];
}

const initialState: OrderState = {
    myOrders: [],
    allOrders: [],
    status: 'idle',
    mutationStatus: 'idle',
    error: null,
    lastPlacedOrder: null,
    pendingOrderIds: [],
};

type ThunkConfig = { state: RootState };

export const placeOrder = createAsyncThunk<OrderResponse, void, ThunkConfig>(
    'orders/placeOrder',
    async (_, { getState, rejectWithValue }) => {
        try {
            return await orderApi.placeOrder(getState().auth.token);
        } catch (err) {
            return rejectWithValue(err instanceof Error ? err.message : 'Failed to place order');
        }
    }
);

export const fetchMyOrders = createAsyncThunk<OrderResponse[], void, ThunkConfig>(
    'orders/fetchMyOrders',
    async (_, { getState, rejectWithValue }) => {
        try {
            return await orderApi.getMyOrders(getState().auth.token);
        } catch (err) {
            return rejectWithValue(err instanceof Error ? err.message : 'Failed to load orders');
        }
    }
);

export const cancelOrder = createAsyncThunk<OrderResponse, number, ThunkConfig>(
    'orders/cancelOrder',
    async (orderId, { getState, rejectWithValue }) => {
        try {
            return await orderApi.cancelOrder(getState().auth.token, orderId);
        } catch (err) {
            return rejectWithValue(err instanceof Error ? err.message : 'Failed to cancel order');
        }
    }
);

export const fetchAllOrders = createAsyncThunk<OrderResponse[], void, ThunkConfig>(
    'orders/fetchAllOrders',
    async (_, { getState, rejectWithValue }) => {
        try {
            return await orderApi.getAllOrders(getState().auth.token);
        } catch (err) {
            return rejectWithValue(err instanceof Error ? err.message : 'Failed to load orders');
        }
    }
);

export const advanceOrderStatus = createAsyncThunk<
    OrderResponse,
    { orderId: number; status: OrderStatus },
    ThunkConfig
>('orders/advanceOrderStatus', async ({ orderId, status }, { getState, rejectWithValue }) => {
    try {
        return await orderApi.advanceOrderStatus(getState().auth.token, orderId, status);
    } catch (err) {
        return rejectWithValue(err instanceof Error ? err.message : 'Failed to update status');
    }
});

export const assignRider = createAsyncThunk <
    OrderResponse,
    { orderId: number; riderPhone: string; deliveryNote?: string },
    ThunkConfig
>('orders/assignRider', async ({ orderId, riderPhone, deliveryNote }, { getState, rejectWithValue }) => {
    try {
        return await orderApi.assignRider(getState().auth.token, orderId, riderPhone, deliveryNote);
    } catch (err) {
        return rejectWithValue(err instanceof Error ? err.message : 'Failed to assign rider');
    }
});

function addPending(state: OrderState, id: number) {
    if (!state.pendingOrderIds.includes(id)) state.pendingOrderIds.push(id);
}
function removePending(state: OrderState, id: number) {
    state.pendingOrderIds = state.pendingOrderIds.filter((i) => i !== id);
}

export const orderSlice = createSlice({
    name: 'orders',
    initialState,
    reducers: {
        clearLastPlacedOrder: (state) => {
            state.lastPlacedOrder = null;
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(placeOrder.pending, (state) => {
                state.mutationStatus = 'loading';
                state.error = null;
            })
            .addCase(placeOrder.fulfilled, (state, action) => {
                state.mutationStatus = 'idle';
                state.lastPlacedOrder = action.payload;
                state.myOrders.unshift(action.payload);
            })
            .addCase(placeOrder.rejected, (state, action) => {
                state.mutationStatus = 'failed';
                state.error = (action.payload as string) ?? 'Failed to place order';
            })
            .addCase(fetchMyOrders.pending, (state) => {
                state.status = 'loading';
                state.error = null;
            })
            .addCase(fetchMyOrders.fulfilled, (state, action) => {
                state.status = 'succeeded';
                state.myOrders = action.payload;
            })
            .addCase(fetchMyOrders.rejected, (state, action) => {
                state.status = 'failed';
                state.error = (action.payload as string) ?? 'Failed to load orders';
            })
            .addCase(fetchAllOrders.pending, (state) => {
                state.status = 'loading';
                state.error = null;
            })
            .addCase(fetchAllOrders.fulfilled, (state, action) => {
                state.status = 'succeeded';
                state.allOrders = action.payload;
            })
            .addCase(fetchAllOrders.rejected, (state, action) => {
                state.status = 'failed';
                state.error = (action.payload as string) ?? 'Failed to load orders';
            })
            .addCase(cancelOrder.pending, (state, action) => addPending(state, action.meta.arg))
            .addCase(cancelOrder.fulfilled, (state, action) => {
                removePending(state, action.payload.orderId);
                const idx = state.myOrders.findIndex((o) => o.orderId === action.payload.orderId);
                if (idx !== -1) state.myOrders[idx] = action.payload;
            })
            .addCase(cancelOrder.rejected, (state, action) => {
                removePending(state, action.meta.arg);
                state.error = (action.payload as string) ?? 'Failed to cancel order';
            })
            .addCase(advanceOrderStatus.pending, (state, action) => addPending(state, action.meta.arg.orderId))
            .addCase(advanceOrderStatus.fulfilled, (state, action) => {
                removePending(state, action.payload.orderId);
                const idx = state.allOrders.findIndex((o) => o.orderId === action.payload.orderId);
                if (idx !== -1) state.allOrders[idx] = action.payload;
            })
            .addCase(advanceOrderStatus.rejected, (state, action) => {
                removePending(state, action.meta.arg.orderId);
                state.error = (action.payload as string) ?? 'Failed to update status';
            })
            .addCase(assignRider.pending, (state, action) => addPending(state, action.meta.arg.orderId))
            .addCase(assignRider.fulfilled, (state, action) => {
                removePending(state, action.payload.orderId);
                const idx = state.allOrders.findIndex((o) => o.orderId === action.payload.orderId);
                if (idx !== -1) state.allOrders[idx] = action.payload;
            })
            .addCase(assignRider.rejected, (state, action) => {
                removePending(state, action.meta.arg.orderId);
                state.error = (action.payload as string) ?? 'Failed to assign rider';
            });
    },
});

export const { clearLastPlacedOrder } = orderSlice.actions;
export default orderSlice.reducer;