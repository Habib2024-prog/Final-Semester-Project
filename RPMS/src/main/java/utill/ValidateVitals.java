package utill;

public class ValidateVitals {
public static  void ValidateVitalsSign(double hr,double oxl,double temp,String bp) throws VitalException {
    if(hr<30||hr>200)throw new VitalException("HeartRate is out of range:"+hr);
    if(oxl<50||oxl>100)throw new VitalException("OxygenLevel is out of range:"+oxl);
    if(temp<34||temp>42)throw new VitalException("Temperature is out of range:"+temp);
    if(!bp.matches("\\d{2,3}/\\d{2,3}"))throw  new VitalException("BloodPressure is invalid formate(eg.120/80)"+bp);

}

}
