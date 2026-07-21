import { useEffect, useRef } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { type RootState, type AppDispatch } from '../../store/store';
import { checkPaymentStatus, setPollTimeout } from './paymentSlice';

const POLL_INTERVAL_MS = 3000;
const MAX_POLLS = 20; // ~60 seconds before giving up and telling the user to check back later

export function usePaymentPolling(orderId: number | null) {
    const dispatch = useDispatch<AppDispatch>();
    const pollStatus = useSelector((state: RootState) => state.payments.pollStatus);
    const pollCount = useRef(0);

    useEffect(() => {
        if (!orderId || pollStatus !== 'polling') return;

        pollCount.current = 0;
        const interval = setInterval(() => {
            pollCount.current += 1;
            if (pollCount.current > MAX_POLLS) {
                dispatch(setPollTimeout());
                clearInterval(interval);
                return;
            }
            dispatch(checkPaymentStatus(orderId));
        }, POLL_INTERVAL_MS);

        return () => clearInterval(interval);
    }, [orderId, pollStatus, dispatch]);
}