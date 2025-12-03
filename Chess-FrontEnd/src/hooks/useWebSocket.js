import { useEffect, useState, useRef } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

const useWebSocket = (topic, onMessageReceived) => {
    const [isConnected, setIsConnected] = useState(false);
    const stompClient = useRef(null);
    const subscription = useRef(null);

    useEffect(() => {
        const socket = new SockJS('http://localhost:8080/ws');
        stompClient.current = Stomp.over(socket);

        const connect = () => {
            stompClient.current.connect({}, (frame) => {
                setIsConnected(true);
                console.log('Connected to WebSocket:', frame);
                if (topic) {
                    subscription.current = stompClient.current.subscribe(topic, (message) => {
                        onMessageReceived(JSON.parse(message.body));
                    });
                }
            }, (error) => {
                console.error('WebSocket connection error:', error);
                setIsConnected(false);
                // Attempt to reconnect after a delay
                setTimeout(connect, 5000);
            });
        };

        connect();

        return () => {
            if (subscription.current) {
                subscription.current.unsubscribe();
            }
            if (stompClient.current && stompClient.current.connected) {
                stompClient.current.disconnect(() => {
                    console.log('Disconnected from WebSocket');
                    setIsConnected(false);
                });
            }
        };
    }, [topic, onMessageReceived]);

    const sendMessage = (destination, body) => {
        if (stompClient.current && stompClient.current.connected) {
            stompClient.current.send(destination, {}, JSON.stringify(body));
        } else {
            console.warn('Cannot send message: WebSocket not connected.');
        }
    };

    return { isConnected, sendMessage };
};

export default useWebSocket;
