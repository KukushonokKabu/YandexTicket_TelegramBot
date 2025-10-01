package ru.mydomain;

public class ElementInfo {
    private final String mainText;
    private final String additionalText;

    public ElementInfo(String mainText,String additionalText){
        this.mainText = mainText;
        this.additionalText = additionalText == null ? "" : additionalText;
    }
    public String getMainText(){return mainText;}
    public String getAdditionalText(){return  additionalText;}
    public boolean hasAdditionalText (){
        return additionalText != null && !additionalText.isEmpty();
    }
    @Override
    public String toString(){
        return hasAdditionalText() ?
                mainText + " ("+ additionalText+")":
                mainText;
    }

}
