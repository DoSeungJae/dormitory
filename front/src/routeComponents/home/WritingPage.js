import React, { useState } from 'react';
import 'bootstrap/dist/css/bootstrap.css'; 
import { Dropdown, DropdownButton } from 'react-bootstrap';
import axios from 'axios';
import {useNavigate} from 'react-router-dom';

function WritingPage() {
    const [title, setTitle] = useState("");
    const [content, setContent] = useState("");
    const [dorSelect, setDorSelect] = useState("기숙사");
    const [cateSelect, setCateSelect]=useState("카테고리");
    const token=localStorage.getItem('token');
    const navigate=useNavigate();

    const dormitoryToId = {
        "오름1": 1,
        "오름2": 2,
        "오름3": 3, 
        "푸름1": 4,
        "푸름2": 5,
        "푸름3": 6,
        "푸름4": 7
      };

    const buttonPressed = async () => {
        //예외처리
        if(title==="" || content==="" || dorSelect==="기숙사" || cateSelect==="카테고리"){
            alert("입력하지 않은 곳이 있어요! 다시 한번 확인해주세요.")
        }
        //서버로 전송 ,토큰을 헤더에 담아서 보내야함 -> 유저 정보 파싱
        const curTime=nowLocalDateTime();

        const fullPath = `http://localhost:8080/api/v1/article/new`;
        const data = {
          dorId: dormitoryToId[dorSelect],
          category:cateSelect,
          title:title,
          content:content,
          createTime:curTime
          // 여기에 보내고 싶은 데이터를 JSON 형식으로 추가하세요.
        };
      
        try {
        const response = await axios.post(fullPath, data, {
            headers: {
            'Authorization':`${token}`,
            }
        });


        } catch (error) {
            if(error.response.data==="유효하지 않은 토큰입니다."){
                alert("회원 정보가 유요하지 않아요! 로그인해주세요.");
                navigate('/logIn',{state:{from:"/newWriting"}});
            }
        }
        

        

    }

    const nowLocalDateTime=()=>{
        const now=new Date();
        const localDateTime = now.getFullYear() + '-' +
        String(now.getMonth() + 1).padStart(2, '0') + '-' +
        String(now.getDate()).padStart(2, '0') + 'T' +
        String(now.getHours()).padStart(2, '0') + ':' +
        String(now.getMinutes()).padStart(2, '0') + ':' +
        String(now.getSeconds()).padStart(2, '0');
        
        return localDateTime;
    }
    
    return (
        <div className="App">

            <header className="App-writingPage-header">
                    <h6>글 쓰기</h6> 
                    <button type="button" className='btn btn-outline-primary'onClick={buttonPressed}>작성</button>
            </header>
                                
            <main className="App-main">
        
                <input type="text" value={title} placeholder='제목'  style={{border:'none',outline:'none',width:'90%'}} onChange={e => setTitle(e.target.value)}  />
        
                <br/>
                <br/>
        
                <textarea value={content} placeholder='내용을 입력하세요.' style={{border:'none',outline:'none',width:'90%',height:'50%'}} onChange={e => setContent(e.target.value)}  />
                <br />
            </main>


            <div className="selects">
                <div>
                    <DropdownButton id="dropdown-item-button"  title={dorSelect} drop="up">
                    <Dropdown.Item as="button" ><div onClick={() => setDorSelect('기숙사')}>기숙사</div></Dropdown.Item>
                        <Dropdown.Item as="button" ><div onClick={() => setDorSelect('오름1')}>오름1</div></Dropdown.Item>
                        <Dropdown.Item as="button"><div onClick={() => setDorSelect('오름2')}>오름2</div></Dropdown.Item>
                        <Dropdown.Item as="button"><div onClick={() => setDorSelect('오름3')}>오름3</div></Dropdown.Item>
                        <Dropdown.Item as="button"><div onClick={() => setDorSelect('푸름1')}>푸름1</div></Dropdown.Item>
                        <Dropdown.Item as="button"><div onClick={() => setDorSelect('푸름2')}>푸름2</div></Dropdown.Item>
                        <Dropdown.Item as="button"><div onClick={() => setDorSelect('푸름3')}>푸름3</div></Dropdown.Item>
                        <Dropdown.Item as="button"><div onClick={() => setDorSelect('푸름4')}>푸름4</div></Dropdown.Item>
                    </DropdownButton>
                </div>

                <div>
                    <DropdownButton id="dropdown-item-button" title={cateSelect} drop="up">
                    <Dropdown.Item as="button"><div onClick={() => setCateSelect('카테고리')}>카테고리</div> </Dropdown.Item>
                        <Dropdown.Item as="button"><div onClick={() => setCateSelect('족발•보쌈')}>족발•보쌈</div> </Dropdown.Item>
                        <Dropdown.Item as="button"><div onClick={() => setCateSelect('찜•탕•찌개')}>찜•탕•찌개</div> </Dropdown.Item>
                        <Dropdown.Item as="button"><div onClick={() => setCateSelect('돈까스•일식')}>돈까스•일식</div> </Dropdown.Item>
                        <Dropdown.Item as="button"><div onClick={() => setCateSelect('피자')}> 피자</div> </Dropdown.Item>
                        <Dropdown.Item as="button"><div onClick={() => setCateSelect('고기•구이')}>고기•구이</div> </Dropdown.Item>
                        <Dropdown.Item as="button"><div onClick={() => setCateSelect('백반•죽•국수')}>백반•죽•국수</div> </Dropdown.Item>
                        <Dropdown.Item as="button"><div onClick={() => setCateSelect('양식')}>양식</div> </Dropdown.Item>
                        <Dropdown.Item as="button"><div onClick={() => setCateSelect('치킨')}>치킨</div> </Dropdown.Item>
                        <Dropdown.Item as="button"><div onClick={() => setCateSelect('중식')}>중식</div> </Dropdown.Item>
                        <Dropdown.Item as="button"><div onClick={() => setCateSelect('아시안')}>아시안</div> </Dropdown.Item>
                        <Dropdown.Item as="button"><div onClick={() => setCateSelect('도시락')}>도시락</div> </Dropdown.Item>
                        <Dropdown.Item as="button"><div onClick={() => setCateSelect('분식')}>분식</div> </Dropdown.Item>
                        <Dropdown.Item as="button"><div onClick={() => setCateSelect('카페•디저트')}>카페•디저트</div> </Dropdown.Item>
                        <Dropdown.Item as="button"><div onClick={() => setCateSelect('패스트푸드')}>패스트푸드</div> </Dropdown.Item>    
                    </DropdownButton>
                </div>
            </div>
        </div>
        

    );
}

export default WritingPage;
