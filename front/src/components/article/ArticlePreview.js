import {React,useContext} from 'react';
import { goArticlePage,calculateDorItemStyle,dorIdToDorName } from '../home/HomeUtils';
import HomeSelectContext from '../home/HomeSelectContext';

function ArticlePreview({articleList,articleListRef,page,isEndPage,dorId,heightStyle}){
                          
  const token=localStorage.getItem('token');
  const {selectComponentIndex,setSelectComponentIndex}=useContext(HomeSelectContext);
    
    return (
        <div className="preview" style={heightStyle} ref={articleListRef}>
              
        {articleList===null && <h3>아직 글이 없어요!</h3>}
        
        {articleList && articleList.map((article, index) => (
        <div key={index} className="article-item" 
        onClick={() => goArticlePage(article,dorId,isEndPage,
                                    page,token,
                                    setSelectComponentIndex,
                                    selectComponentIndex)}>
          <div className='article-item-summary'>
            <div className="article-item-text" 
                  id='article-item-title'>
                    {article.title}
            </div>
            <div className="article-item-text"
                  id='article-item-content'>
                    {article.content}
            </div>
          </div>
          <div className='article-item-icons'>
            <div className='dor-icon'
              style={calculateDorItemStyle(dorId-1,article.dorId-1)}>
              {dorIdToDorName[article.dorId]}
            </div>
          </div>
        </div>
      ))}

      </div>

    )
    

}

export default ArticlePreview