/*
 * Copyright 2025 logongas.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.logongas.ix3.web.json.beanmapper;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author logongas
 */
public class PropertyNameListTest {
    
    public PropertyNameListTest() {
    }



    @Test
    public void testToString() {
        System.out.println("toString");
        String expResult = "a,b,c";
        assertEquals(expResult, new PropertyNameList(expResult, null).toString());
    }
    @Test
    public void testToString1() {
        System.out.println("toString1");
        String expResult = "a";
        assertEquals(expResult, new PropertyNameList(expResult, null).toString());
    }
    @Test
    public void testToString2() {
        System.out.println("toString2");
        String expResult = "a>,b,c";
        assertEquals(expResult, new PropertyNameList(expResult, null).toString());
    }
    @Test
    public void testToString3() {
        System.out.println("toString3");
        String expResult = "a,<b,c";
        assertEquals(expResult, new PropertyNameList(expResult, null).toString());
    }
    @Test
    public void testToString4() {
        System.out.println("toString4");
        String expResult = "a,<b,c>";
        assertEquals(expResult, new PropertyNameList(expResult, null).toString());
    }
    @Test
    public void testToString5() {
        System.out.println("toString5");
        String expResult = "<a,<b,c";
        assertEquals(expResult, new PropertyNameList(expResult, null).toString());
    }
    @Test
    public void testToString6() {
        System.out.println("toString6");
        String expResult = "<a,b,c>";
        assertEquals(expResult, new PropertyNameList(expResult, null).toString());
    }    
    @Test
    public void testToString7() {
        System.out.println("toString7");
        String propertyNames="<a>";
        String expResult = "a";
        assertEquals(expResult, new PropertyNameList(propertyNames, null).toString());
    } 
    @Test
    public void testToString8() {
        System.out.println("toString8");
        String propertyNames="<a,b>,<c>,d";
        String expResult = "<a,b>,c,d";
        assertEquals(expResult, new PropertyNameList(propertyNames, null).toString());
    }    
    @Test
    public void testToString9() {
        System.out.println("toString9");
        String propertyNames="<a,b>,<c>,d,";
        String expResult = "<a,b>,c,d";
        assertEquals(expResult, new PropertyNameList(propertyNames, null).toString());
    } 
    @Test
    public void testToString10() {
        System.out.println("toString10");
        String propertyNames="a,b,a,b";
        String expResult = "a,b";
        assertEquals(expResult, new PropertyNameList(propertyNames, null).toString());
    }     
    @Test
    public void testToString11() {
        System.out.println("toString11");
        String propertyNames="a,b>,a,<b";
        String expResult = "a,b";
        assertEquals(expResult, new PropertyNameList(propertyNames).toString());
    }    
    @Test
    public void testToString12() {
        System.out.println("toString12");
        String propertyNames="a,<b,a,<b";
        String expResult = "a,<b";
        assertEquals(expResult, new PropertyNameList(propertyNames).toString());
    } 
    @Test
    public void testToString13() {
        System.out.println("toString13");
        String propertyNames="a,<b>,a,b>";
        String expResult = "a,b";
        assertEquals(expResult, new PropertyNameList(propertyNames).toString());
    }     
    @Test
    public void testToString14() {
        System.out.println("toString14");
        String propertyNames="a,b>,a,b>";
        String expResult = "a,b>";
        assertEquals(expResult, new PropertyNameList(propertyNames).toString());
    }      
    
    @Test
    public void testToStringP() {
        System.out.println("toStringP");
        String propertyNames = "a,b,c";
        String expResult = "p.a,p.b,p.c";
        assertEquals(expResult, new PropertyNameList(propertyNames, "p").toString());
    }
    @Test
    public void testToString1P() {
        System.out.println("toString1P");
        String propertyNames = "a";
        String expResult = "p.a";
        assertEquals(expResult, new PropertyNameList(propertyNames, "p").toString());
    }
    @Test
    public void testToString2P() {
        System.out.println("toString2P");
        String propertyNames = "a>,b,c";
        String expResult = "p.a>,p.b,p.c";
        assertEquals(expResult, new PropertyNameList(propertyNames, "p").toString());
    }
    @Test
    public void testToString3P() {
        System.out.println("toString3P");
        String propertyNames = "a,<b,c";
        String expResult = "p.a,<p.b,p.c";
        assertEquals(expResult, new PropertyNameList(propertyNames, "p").toString());
    }
    @Test
    public void testToString4P() {
        System.out.println("toString4P");
        String propertyNames = "a,<b,c>";
        String expResult = "p.a,<p.b,p.c>";
        assertEquals(expResult, new PropertyNameList(propertyNames, "p").toString());
    }
    @Test
    public void testToString5P() {
        System.out.println("toString5P");
        String propertyNames = "<a,<b,c";
        String expResult = "<p.a,<p.b,p.c";
        assertEquals(expResult, new PropertyNameList(propertyNames, "p").toString());
    }
    @Test
    public void testToString6P() {
        System.out.println("toString6P");
        String propertyNames = "<a,b,c>";
        String expResult = "<p.a,p.b,p.c>";
        assertEquals(expResult, new PropertyNameList(propertyNames, "p").toString());
    }    
    @Test
    public void testToString7P() {
        System.out.println("toString7P");
        String propertyNames="<a>";
        String expResult = "p.a";
        assertEquals(expResult, new PropertyNameList(propertyNames, "p").toString());
    } 
    @Test
    public void testToString8P() {
        System.out.println("toString8P");
        String propertyNames="<a,b>,<c>,d";
        String expResult = "<p.a,p.b>,p.c,p.d";
        assertEquals(expResult, new PropertyNameList(propertyNames, "p").toString());
    }    
    @Test
    public void testToString9P() {
        System.out.println("toString9P");
        String propertyNames="<a,b>,<c>,d,";
        String expResult = "<p.a,p.b>,p.c,p.d";
        assertEquals(expResult, new PropertyNameList(propertyNames, "p").toString());
    } 
    

    
    
    @Test
    public void testGetPropertyDirection1() {
        System.out.println("getPropertyDirection1");
        String propertyNames="a>";
        String propertyName="a";
        PropertyDirection expResult=PropertyDirection.IN_TO_SERVER;
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyDirection(propertyName));
    }    
    @Test
    public void testGetPropertyDirection2() {
        System.out.println("getPropertyDirection2");
        String propertyNames="<a";
        String propertyName="a";
        PropertyDirection expResult=PropertyDirection.OUT_FROM_SERVER;
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyDirection(propertyName));
    }  
    @Test
    public void testGetPropertyDirection21() {
        System.out.println("getPropertyDirection21");
        String propertyNames="<a,b";
        String propertyName="a";
        PropertyDirection expResult=PropertyDirection.OUT_FROM_SERVER;
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyDirection(propertyName));
    }     
    @Test
    public void testGetPropertyDirection22() {
        System.out.println("getPropertyDirection22");
        String propertyNames="<a,b>";
        String propertyName="a";
        PropertyDirection expResult=PropertyDirection.OUT_FROM_SERVER;
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyDirection(propertyName));
    }     
    @Test
    public void testGetPropertyDirection23() {
        System.out.println("getPropertyDirection23");
        String propertyNames="b>,<a";
        String propertyName="a";
        PropertyDirection expResult=PropertyDirection.OUT_FROM_SERVER;
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyDirection(propertyName));
    }     
    
    
    @Test
    public void testGetPropertyDirection3() {
        System.out.println("getPropertyDirection3");
        String propertyNames="a";
        String propertyName="a";
        PropertyDirection expResult=PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyDirection(propertyName));
    }
    @Test
    public void testGetPropertyDirection31() {
        System.out.println("getPropertyDirection31");
        String propertyNames="a,b>,<c";
        String propertyName="a";
        PropertyDirection expResult=PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyDirection(propertyName));
    }  
    @Test
    public void testGetPropertyDirection32() {
        System.out.println("getPropertyDirection32");
        String propertyNames="b>,a";
        String propertyName="a";
        PropertyDirection expResult=PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyDirection(propertyName));
    }
    @Test
    public void testGetPropertyDirection33() {
        System.out.println("getPropertyDirection33");
        String propertyNames="b,a,c";
        String propertyName="a";
        PropertyDirection expResult=PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyDirection(propertyName));
    }
    @Test
    public void testGetPropertyDirection34() {
        System.out.println("getPropertyDirection34");
        String propertyNames="b>,<c,a";
        String propertyName="a";
        PropertyDirection expResult=PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyDirection(propertyName));
    }    
    
    
    
    @Test
    public void testGetPropertyDirection4() {
        System.out.println("getPropertyDirection4");
        String propertyNames="<a>";
        String propertyName="a";
        PropertyDirection expResult=PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyDirection(propertyName));
    }    
    @Test
    public void testGetPropertyDirection41() {
        System.out.println("getPropertyDirection41");
        String propertyNames="<a>,b";
        String propertyName="a";
        PropertyDirection expResult=PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyDirection(propertyName));
    }
    @Test
    public void testGetPropertyDirection42() {
        System.out.println("getPropertyDirection42");
        String propertyNames="b,<a>";
        String propertyName="a";
        PropertyDirection expResult=PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyDirection(propertyName));
    }
    @Test
    public void testGetPropertyDirection43() {
        System.out.println("getPropertyDirection43");
        String propertyNames="b,c,<a>";
        String propertyName="a";
        PropertyDirection expResult=PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyDirection(propertyName));
    }
    @Test
    public void testGetPropertyDirection44() {
        System.out.println("getPropertyDirection44");
        String propertyNames="<a>,b,c";
        String propertyName="a";
        PropertyDirection expResult=PropertyDirection.IN_TO_SERVER_OUT_FROM_SERVER;
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyDirection(propertyName));
    }    
    
    
    @Test
    public void testGetPropertyNamesFiltered() {
        System.out.println("getPropertyNamesFiltered");
        String propertyNames="<a>,<b,c,d>";
        List<String> expResult=Arrays.asList("a","c");
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyNamesFiltered(Boolean.TRUE, Boolean.TRUE));
    }     
    @Test
    public void testGetPropertyNamesFilteredP() {
        System.out.println("getPropertyNamesFilteredP");
        String propertyNames="<a>,<b,c,d>";
        List<String> expResult=Arrays.asList("p.a","p.c");
        assertEquals(expResult,new PropertyNameList(propertyNames, "p").getPropertyNamesFiltered(Boolean.TRUE, Boolean.TRUE));
    }
    
    @Test
    public void testGetPropertyNamesFiltered2() {
        System.out.println("getPropertyNamesFiltered2");
        String propertyNames="<a>,<b,c,d>";
        List<String> expResult=Arrays.asList("d");
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyNamesFiltered(Boolean.TRUE, Boolean.FALSE));
    }     
    @Test
    public void testGetPropertyNamesFiltered2P() {
        System.out.println("getPropertyNamesFiltered2P");
        String propertyNames="<a>,<b,c,d>";
        List<String> expResult=Arrays.asList("p.d");
        assertEquals(expResult,new PropertyNameList(propertyNames, "p").getPropertyNamesFiltered(Boolean.TRUE, Boolean.FALSE));
    }

    @Test
    public void testGetPropertyNamesFiltered3() {
        System.out.println("getPropertyNamesFiltered3");
        String propertyNames="<a>,<b,c,d>";
        List<String> expResult=Arrays.asList("b");
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyNamesFiltered(Boolean.FALSE, Boolean.TRUE));
    }     
    @Test
    public void testGetPropertyNamesFiltered3P() {
        System.out.println("getPropertyNamesFiltered3P");
        String propertyNames="<a>,<b,c,d>";
        List<String> expResult=Arrays.asList("p.b");
        assertEquals(expResult,new PropertyNameList(propertyNames, "p").getPropertyNamesFiltered(Boolean.FALSE, Boolean.TRUE));
    }

    @Test
    public void testGetPropertyNamesFiltered4() {
        System.out.println("getPropertyNamesFiltered4");
        String propertyNames="<a>,<b,c,d>";
        List<String> expResult=Arrays.asList();
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyNamesFiltered(Boolean.FALSE, Boolean.FALSE));
    }     
    @Test
    public void testGetPropertyNamesFiltered4P() {
        System.out.println("getPropertyNamesFiltered4P");
        String propertyNames="<a>,<b,c,d>";
        List<String> expResult=Arrays.asList();
        assertEquals(expResult,new PropertyNameList(propertyNames, "p").getPropertyNamesFiltered(Boolean.FALSE, Boolean.FALSE));
    }    
    
    
    @Test
    public void testGetPropertyNamesFiltered5() {
        System.out.println("getPropertyNamesFiltered5");
        String propertyNames="<a>,<b,c,d>";
        List<String> expResult=Arrays.asList("b");
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyNamesFiltered(Boolean.FALSE, null));
    }     
    @Test
    public void testGetPropertyNamesFiltered5P() {
        System.out.println("getPropertyNamesFiltered5P");
        String propertyNames="<a>,<b,c,d>";
        List<String> expResult=Arrays.asList("p.b");
        assertEquals(expResult,new PropertyNameList(propertyNames, "p").getPropertyNamesFiltered(Boolean.FALSE, null));
    }    


    @Test
    public void testGetPropertyNamesFiltered6() {
        System.out.println("getPropertyNamesFiltered6");
        String propertyNames="<a>,<b,c,d>";
        List<String> expResult=Arrays.asList("a","c","d");
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyNamesFiltered(Boolean.TRUE, null));
    }     
    @Test
    public void testGetPropertyNamesFiltered6P() {
        System.out.println("getPropertyNamesFiltered6P");
        String propertyNames="<a>,<b,c,d>";
        List<String> expResult=Arrays.asList("p.a","p.c","p.d");
        assertEquals(expResult,new PropertyNameList(propertyNames, "p").getPropertyNamesFiltered(Boolean.TRUE, null));
    }    

    @Test
    public void testGetPropertyNamesFiltered7() {
        System.out.println("getPropertyNamesFiltered7");
        String propertyNames="<a>,<b,c,d>";
        List<String> expResult=Arrays.asList("d");
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyNamesFiltered(null, Boolean.FALSE));
    }     
    @Test
    public void testGetPropertyNamesFiltered7P() {
        System.out.println("getPropertyNamesFiltered7P");
        String propertyNames="<a>,<b,c,d>";
        List<String> expResult=Arrays.asList("p.d");
        assertEquals(expResult,new PropertyNameList(propertyNames, "p").getPropertyNamesFiltered(null, Boolean.FALSE));
    }    

    @Test
    public void testGetPropertyNamesFiltered8() {
        System.out.println("getPropertyNamesFiltered8");
        String propertyNames="<a>,<b,c,d>";
        List<String> expResult=Arrays.asList("a","b","c");
        assertEquals(expResult,new PropertyNameList(propertyNames, null).getPropertyNamesFiltered(null, Boolean.TRUE));
    }     
    @Test
    public void testGetPropertyNamesFiltered8P() {
        System.out.println("getPropertyNamesFiltered8P");
        String propertyNames="<a>,<b,c,d>";
        List<String> expResult=Arrays.asList("p.a","p.b","p.c");
        assertEquals(expResult,new PropertyNameList(propertyNames, "p").getPropertyNamesFiltered(null, Boolean.TRUE));
    }        
    
   

    @Test
    public void testAppendOrReplace1() {
        System.out.println("AppendOrReplace1");
        String propertyNames1="a,b,c";
        String propertyNames2="d,e,f";
        String expResult="a,b,c,d,e,f";
        assertEquals(expResult,new PropertyNameList(propertyNames1).appendOrReplace(new PropertyNameList(propertyNames2)).toString());
    }    
        
    @Test
    public void testAppendOrReplace2() {
        System.out.println("AppendOrReplace2");
        String propertyNames1="a,b,c";
        String propertyNames2="d,e,f";
        String expResult="p.a,p.b,p.c,p.d,p.e,p.f";
        assertEquals(expResult,new PropertyNameList(propertyNames1,"p").appendOrReplace(new PropertyNameList(propertyNames2,"p")).toString());
    } 
    
    
    @Test
    public void testAppendOrReplace3() {
        System.out.println("AppendOrReplace3");
        String propertyNames1="a,b,c";
        String propertyNames2="d,e,f";
        String expResult="p.a,p.b,p.c,q.d,q.e,q.f";
        assertEquals(expResult,new PropertyNameList(propertyNames1,"p").appendOrReplace(new PropertyNameList(propertyNames2,"q")).toString());
    }   
    @Test
    public void testAppendOrReplace4() {
        System.out.println("AppendOrReplace4");
        String propertyNames1="a,b,c";
        String propertyNames2="d,e,f";
        String expResult="a,b,c,q.d,q.e,q.f";
        assertEquals(expResult,new PropertyNameList(propertyNames1).appendOrReplace(new PropertyNameList(propertyNames2,"q")).toString());
    } 
    
    
    @Test
    public void testAppendOrReplace5() {
        System.out.println("AppendOrReplace5");
        String propertyNames1="a,b,c";
        String propertyNames2="a,b,c";
        String expResult="p.a,p.b,p.c,q.a,q.b,q.c";
        assertEquals(expResult,new PropertyNameList(propertyNames1,"p").appendOrReplace(new PropertyNameList(propertyNames2,"q")).toString());
    } 
    
    
    @Test
    public void testAppendOrReplace6() {
        System.out.println("AppendOrReplace6");
        String propertyNames1="a,b,c";
        String propertyNames2="a,b,c";
        String expResult="a,b,c";
        assertEquals(expResult,new PropertyNameList(propertyNames1).appendOrReplace(new PropertyNameList(propertyNames2)).toString());
    } 
        
    @Test
    public void testAppendOrReplace7() {
        System.out.println("AppendOrReplace8");
        String propertyNames1="a,b,c,c1";
        String propertyNames2="a,b,c2";
        String expResult="a,b,c,c1,c2";
        assertEquals(expResult,new PropertyNameList(propertyNames1).appendOrReplace(new PropertyNameList(propertyNames2)).toString());
    }
    
    @Test
    public void testAppendOrReplace8() {
        System.out.println("AppendOrReplace8");
        String propertyNames1="a";
        String propertyNames2="b";
        String expResult="a,p.b";
        assertEquals(expResult,new PropertyNameList(propertyNames1).appendOrReplace(new PropertyNameList(propertyNames2,"p")).toString());
    }    
    @Test
    public void testAppendOrReplace9() {
        System.out.println("AppendOrReplace9");
        String propertyNames1="a>";
        String propertyNames2="<a";
        String expResult="<a";
        assertEquals(expResult,new PropertyNameList(propertyNames1).appendOrReplace(new PropertyNameList(propertyNames2)).toString());
    }      
    @Test
    public void testAppendOrReplace10() {
        System.out.println("AppendOrReplace10");
        String propertyNames1="a,b,c>";
        String propertyNames2="<d";
        String expResult="a,b,c>,<d";
        assertEquals(expResult,new PropertyNameList(propertyNames1).appendOrReplace(new PropertyNameList(propertyNames2)).toString());
    }  
    @Test
    public void testAppendOrReplace11() {
        System.out.println("AppendOrReplace11");
        String propertyNames1="a,b,c";
        String propertyNames2="c>";
        String expResult="a,b,c>";
        assertEquals(expResult,new PropertyNameList(propertyNames1).appendOrReplace(new PropertyNameList(propertyNames2)).toString());
    } 
    @Test
    public void testAppendOrReplace12() {
        System.out.println("AppendOrReplace12");
        String propertyNames1="a,b,c";
        String propertyNames2="<c";
        String expResult="a,b,<c";
        assertEquals(expResult,new PropertyNameList(propertyNames1).appendOrReplace(new PropertyNameList(propertyNames2)).toString());
    }     
    @Test
    public void testAppendOrReplace13() {
        System.out.println("AppendOrReplace13");
        String propertyNames1="a,b>,c>";
        String propertyNames2="<b,<c";
        String expResult="a,<b,<c";
        assertEquals(expResult,new PropertyNameList(propertyNames1).appendOrReplace(new PropertyNameList(propertyNames2)).toString());
    } 
    @Test
    public void testAppendOrReplace14() {
        System.out.println("AppendOrReplace14");
        String propertyNames1="a,<b,<c";
        String propertyNames2="<b,<c";
        String expResult="a,<b,<c";
        assertEquals(expResult,new PropertyNameList(propertyNames1).appendOrReplace(new PropertyNameList(propertyNames2)).toString());
    }   
    @Test
    public void testAppendOrReplace15() {
        System.out.println("AppendOrReplace15");
        String propertyNames1="a,<b>,<c>";
        String propertyNames2="<b,<c";
        String expResult="a,<b,<c";
        assertEquals(expResult,new PropertyNameList(propertyNames1).appendOrReplace(new PropertyNameList(propertyNames2)).toString());
    }    
    
    
    @Test
    public void testRemove1() {
        System.out.println("remove1");
        String propertyNames="a,b,c,d";
        String removePropertyNames="b";
        String expResult="a,c,d";
        assertEquals(expResult,new PropertyNameList(propertyNames).remove(removePropertyNames).toString());
    }  
    @Test
    public void testRemove2() {
        System.out.println("remove2");
        String propertyNames="a";
        String removePropertyNames="a";
        String expResult="";
        assertEquals(expResult,new PropertyNameList(propertyNames).remove(removePropertyNames).toString());
    } 
    @Test
    public void testRemove3() {
        System.out.println("remove3");
        String propertyNames="a,b";
        String removePropertyNames="b";
        String expResult="a";
        assertEquals(expResult,new PropertyNameList(propertyNames).remove(removePropertyNames).toString());
    }
    
    @Test
    public void testRemove4() {
        System.out.println("remove4");
        String propertyNames="a,b";
        String removePropertyNames="b";
        String prefix="p";
        String expResult="p.a,p.b";
        assertEquals(expResult,new PropertyNameList(propertyNames,prefix).remove(removePropertyNames).toString());
    }    
    
    @Test
    public void testRemove5() {
        System.out.println("remove5");
        String propertyNames="a,b";
        String removePropertyNames="p.b";
        String prefix="p";
        String expResult="p.a";
        assertEquals(expResult,new PropertyNameList(propertyNames,prefix).remove(removePropertyNames).toString());
    }     
    @Test(expected = Exception.class)
    public void testRemove6() {
        System.out.println("remove6");
        String propertyNames="a,b";
        String removePropertyNames="c>";
        new PropertyNameList(propertyNames).remove(removePropertyNames).toString();
    }
    @Test(expected = Exception.class)
    public void testRemove7() {
        System.out.println("remove7");
        String propertyNames="a,b";
        String removePropertyNames="<c";
        new PropertyNameList(propertyNames).remove(removePropertyNames).toString();
    }
    
    @Test
    public void testRemove8() {
        System.out.println("remove8");
        String propertyNames="a,b";
        String removePropertyNames="p.a,p.b";
        String prefix="p";        
        String expResult="";
        assertEquals(expResult,new PropertyNameList(propertyNames,prefix).remove(removePropertyNames).toString());
    }

    @Test
    public void testRemove9() {
        System.out.println("remove9");
        String propertyNames="a,b";
        String removePropertyNames="p.a , p.b";
        String prefix="p";        
        String expResult="";
        assertEquals(expResult,new PropertyNameList(propertyNames,prefix).remove(removePropertyNames).toString());
    }
    
    @Test
    public void testRemove10() {
        System.out.println("remove10");
        String propertyNames="a,b";
        String removePropertyNames=",b,";     
        String expResult="a";
        assertEquals(expResult,new PropertyNameList(propertyNames).remove(removePropertyNames).toString());
    } 
    
    @Test
    public void testRemove11() {
        System.out.println("remove11");
        String propertyNames="a,b";
        String removePropertyNames="c,d";     
        String expResult="a,b";
        assertEquals(expResult,new PropertyNameList(propertyNames).remove(removePropertyNames).toString());
    }     
}
