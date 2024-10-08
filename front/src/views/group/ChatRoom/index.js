import { Box, Button, Container, Grid, TextField, Typography } from '@mui/material';
import React, { useEffect, useState } from 'react';

import ChatBubble from '../../../components/group/ChatBubble';
import { useSocket } from '../../../hooks/group/useSocket';
import { getSocketResponse } from '../../../service/group/socket';
import socket from 'socket.io-client/lib/socket';
import { ConnectingAirportsOutlined } from '@mui/icons-material';
import { toast } from 'react-toastify';

function ChatRoom({ username, room,socketResponse,sendData }) {


  const [messageInput, setMessageInput] = useState("");
  const [messageList, setMessageList] = useState([]);

  const addMessageToList = (val) => {
    if (val.room === "") return;
    setMessageList([...messageList]);
    fetchMessage();
  }

  const sendMessage = (e) => {
    e.preventDefault();
    if (messageInput !== "") {
      sendData({
        message: messageInput,
        createdAt: new Date()
      });
      addMessageToList({
        message: messageInput,
        username: username,
        createdAt: new Date(),
        messageType: "CLIENT"
      });
      setMessageInput("");
    }
  }

  const fetchMessage = () => {
    getSocketResponse(room)
            .then((res) => {
                setMessageList([...res]);
            }).catch((err) => {
                console.log(err);
            });
  }

  useEffect(() => {
    fetchMessage();
  }, []);

  useEffect(() => {
    addMessageToList(socketResponse);
  }, [socketResponse]);

  return (
    <div className='App'>
      <div className='group-messages'>
        {
          messageList.map((message) => {
            if (message.messageType === 'CLIENT') {
              return (
                <ChatBubble
                  key={message.id} 
                  isSender={message.username === username}
                  username={message.username}
                  message={message.message}
                  createdTime={message.createdTime}
                />
              )
            } 
          })
        }
      </div>
      <div className='group-form'>
        <TextField 
          variant="standard"
          placeholder='메세지를 입력하세요.'
          value={messageInput}
          onChange={(e) => setMessageInput(e.target.value)}
          fullWidth
          InputProps={{
            disableUnderline: true,
            sx: {
            paddingX: '0.5rem'
            }
          }}
        />

        <Button onClick={(e) => sendMessage(e)}>전송</Button>
      </div>
    </div>
      

  )
}

export default ChatRoom;