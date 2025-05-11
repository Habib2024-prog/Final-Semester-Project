package utill;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FeedBack {
    private  String feedBackText;
    private LocalDate feedBackDate;

    public FeedBack(String feedBackText){
        this(feedBackText,LocalDate.now());
    }
    public FeedBack(String feedBackText ,LocalDate date){
        this.feedBackText=feedBackText;
        this.feedBackDate=date;
    }
    public String getFeedBackText(){return feedBackText;}
    public LocalDate getFeedBackDate(){return feedBackDate;}
    @Override
    public  String toString(){return "FeedBack:"+feedBackText+"\nDate:"+getFeedBackDate();}

}
