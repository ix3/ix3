/*
 * Copyright 2015 logongas.
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
package es.logongas.ix3.web.controllers.endpoint;

import es.logongas.ix3.web.json.beanmapper.BeanMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author logongas
 */
public class EndPointTest {
    
    public EndPointTest() {
    }

    @Test
    public void testCreateEndPoint() {
        System.out.println("createEndPoint");
        BeanMapper beanMapper = new BeanMapper(Object.class);
        EndPoint result = EndPoint.createEndPoint("/pepe", "juan", beanMapper);
        assertEquals("/pepe", result.getPath());
        assertEquals("juan", result.getMethod());
        assertEquals(beanMapper, result.getBeanMapper());
    }
    @Test
    public void testCreateEndPoint2() {
        System.out.println("createEndPoint 2");
        EndPoint result = EndPoint.createEndPoint("/pepe", "juan", null);
        assertEquals("/pepe", result.getPath());
        assertEquals("juan", result.getMethod());
        assertEquals(null, result.getBeanMapper());
    }
    
    @Test
    public void testCreateEndPointCrud_String_Class() {
        System.out.println("createEndPointCrud");
        EndPoint result = EndPoint.createEndPointCrud("/pepe", Object.class);
        assertEquals("/pepe/Object/**", result.getPath());
        assertEquals(null, result.getMethod());
        assertEquals(Object.class, result.getBeanMapper().getEntityClass());
    }

    @Test
    public void testCreateEndPointCrud_String_BeanMapper() {
        System.out.println("createEndPointCrud");
        BeanMapper beanMapper= new BeanMapper(Object.class);
        EndPoint result = EndPoint.createEndPointCrud("/pepe", beanMapper);
        assertEquals("/pepe/Object/**", result.getPath());
        assertEquals(null, result.getMethod());
        assertEquals(beanMapper, result.getBeanMapper());
    }
    
    @Test(expected = Exception.class)
    public void testCreateEndPointCrud_String_BeanMapper2() {
        System.out.println("createEndPointCrud 2");
        EndPoint result = EndPoint.createEndPointCrud("/pepe", (BeanMapper)null);
        assertEquals("/pepe/Object/**", result.getPath());
        assertEquals(null, result.getMethod());
        assertEquals(null, result.getBeanMapper());
    }    

    @Test
    public void testMatches01() {
        System.out.println("matches 01");
        EndPoint endPoint = EndPoint.createEndPoint("/pepe/juan","hola", null);
        assertEquals(true, endPoint.matches("/pepe/juan", "hola"));
    }
    
    @Test
    public void testMatches02() {
        System.out.println("matches 02");
        EndPoint endPoint = EndPoint.createEndPoint("/pepe/juan", null, null);
        assertEquals(true, endPoint.matches("/pepe/juan", "hola"));
    }    
    
    @Test
    public void testMatches03() {
        System.out.println("matches 03");
        EndPoint endPoint = EndPoint.createEndPoint("/pepe/juan", null, null);
        assertEquals(false, endPoint.matches("/pepe/juan/carlos", "hola"));
    } 
    
    @Test
    public void testMatches04() {
        System.out.println("matches 04");
        EndPoint endPoint = EndPoint.createEndPoint("/pepe/juan/**", null, null);
        assertEquals(true, endPoint.matches("/pepe/juan/carlos", "hola"));
    }
    
    @Test
    public void testMatches05() {
        System.out.println("matches 05");
        EndPoint endPoint = EndPoint.createEndPoint("/pepe/juan/**", null, null);
        assertEquals(true, endPoint.matches("/pepe/juan/", "hola"));
    }   
    
    @Test
    public void testMatches06() {
        System.out.println("matches 06");
        EndPoint endPoint = EndPoint.createEndPoint("/pepe/juan/**", null, null);
        assertEquals(true, endPoint.matches("/pepe/juan", "hola"));
    }

    @Test
    public void testMatches07() {
        System.out.println("matches 07");
        EndPoint endPoint = EndPoint.createEndPoint("/pepe/juan", null, null);
        assertEquals(true, endPoint.matches("/pepe/juan", "hola"));
    }
    @Test
    public void testMatches08() {
        System.out.println("matches 08");
        EndPoint endPoint = EndPoint.createEndPoint("/pepe/juan", null, null);
        assertEquals(false, endPoint.matches("/pepe/carlos", "hola"));
    }    
    
    @Test
    public void testMatches09() {
        System.out.println("matches 09");
        EndPoint endPoint = EndPoint.createEndPoint("/pepe/juan", "*", null);
        assertEquals(true, endPoint.matches("/pepe/juan", "hola"));
    } 
    @Test
    public void testMatches10() {
        System.out.println("matches 10");
        EndPoint endPoint = EndPoint.createEndPoint("/pepe/juan", "adios", null);
        assertEquals(false, endPoint.matches("/pepe/juan", "hola"));
    } 
    @Test
    public void testMatches11() {
        System.out.println("matches 11");
        EndPoint endPoint = EndPoint.createEndPoint("/pepe/juan", "adios", null);
        assertEquals(false, endPoint.matches("/pepe/carlos", "hola"));
    }     
    
    
    @Test
    public void testGetMatchEndPoint01() {
        System.out.println("getMatchEndPoint 01");
        List<EndPoint> endPoints = new ArrayList<EndPoint>();
        endPoints.add(EndPoint.createEndPoint("/pepe/juan", "*", null));
        endPoints.add(EndPoint.createEndPoint("/pepe/carlos", "adios", null));
        
        
        List<EndPoint> result = EndPoint.getMatchEndPoint(endPoints,"/pepe/juan" , "GET");
        assertEquals(1, result.size());
        assertEquals(endPoints.get(0), result.get(0));
    }  
    
    @Test
    public void testGetMatchEndPoint02() {
        System.out.println("getMatchEndPoint 02");
        List<EndPoint> endPoints = new ArrayList<EndPoint>();
        endPoints.add(EndPoint.createEndPoint("/pepe/juan", "*", null));
        endPoints.add(EndPoint.createEndPoint("/pepe/juan", "adios", null));
        
        
        List<EndPoint> result = EndPoint.getMatchEndPoint(endPoints,"/pepe/juan" , "adios");
        assertEquals(2, result.size());
        assertEquals(endPoints.get(0), result.get(0));
        assertEquals(endPoints.get(1), result.get(1));
    }     

    
    @Test
    public void testGetMatchEndPoint03() {
        System.out.println("getMatchEndPoint 03");
        List<EndPoint> endPoints = new ArrayList<EndPoint>();
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/**", "adios", null));
        endPoints.add(EndPoint.createEndPoint("/pepe/juan", "adios", null));
        
        
        List<EndPoint> result = EndPoint.getMatchEndPoint(endPoints,"/pepe/juan" , "adios");
        assertEquals(2, result.size());
        assertEquals(endPoints.get(0), result.get(0));
        assertEquals(endPoints.get(1), result.get(1));
    } 
    
    @Test
    public void testGetMatchEndPoint04() {
        System.out.println("getMatchEndPoint 04");
        List<EndPoint> endPoints = new ArrayList<EndPoint>();
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/**", "adios", null));
        endPoints.add(EndPoint.createEndPoint("/pepe/juan", "adios", null));
        
        
        List<EndPoint> result = EndPoint.getMatchEndPoint(endPoints,"/pepe/carlos" , "adios");
        assertEquals(0, result.size());
    }    
    
    @Test
    public void testGetBestEndPoint01() {
        System.out.println("getMatchEndPoint 01");
        List<EndPoint> endPoints = new ArrayList<EndPoint>();
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/**", "adios", null));
        endPoints.add(EndPoint.createEndPoint("/pepe/juan", "adios", null));
        
        
        EndPoint endPoint = EndPoint.getBestEndPoint(endPoints,"/pepe/juan" , "adios");
        assertEquals(endPoints.get(1), endPoint);
    }    
    @Test
    public void testGetBestEndPoint02() {
        System.out.println("getMatchEndPoint 02");
        List<EndPoint> endPoints = new ArrayList<EndPoint>();
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/*", "adios", null));
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/**", "adios", null));
        
        
        EndPoint endPoint = EndPoint.getBestEndPoint(endPoints,"/pepe/juan/pepe.txt" , "adios");
        assertEquals(endPoints.get(0), endPoint);
    } 
    
    @Test(expected = RuntimeException.class)
    public void testGetBestEndPoint03() {
        System.out.println("getMatchEndPoint 03");
        List<EndPoint> endPoints = new ArrayList<EndPoint>();
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/**", "adios", null));
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/**", "adios", null));
        
        
        EndPoint.getBestEndPoint(endPoints,"/pepe/juan" , "adios");
    }  
    
    @Test
    public void testGetBestEndPoint04() {
        System.out.println("getMatchEndPoint 04");
        List<EndPoint> endPoints = new ArrayList<EndPoint>();
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/**", "adios", null));
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/*", "*", null));
        
        
        EndPoint endPoint = EndPoint.getBestEndPoint(endPoints,"/pepe/juan/pepe.txt" , "adios");
        assertEquals(endPoints.get(1), endPoint);
    }
    
    @Test
    public void testGetBestEndPoint05() {
        System.out.println("getMatchEndPoint 05");
        List<EndPoint> endPoints = new ArrayList<EndPoint>();
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/**", null, null));
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/*", "adios", null));
        
        
        EndPoint endPoint = EndPoint.getBestEndPoint(endPoints,"/pepe/juan/pepe.txt" , "adios");
        assertEquals(endPoints.get(1), endPoint);
    } 
    
    @Test
    public void testGetBestEndPoint06() {
        System.out.println("getMatchEndPoint 06");
        List<EndPoint> endPoints = new ArrayList<EndPoint>();
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/*", "*", null));
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/*", null, null));
        
        
        EndPoint endPoint = EndPoint.getBestEndPoint(endPoints,"/pepe/juan/pepe.txt" , "adios");
        assertEquals(endPoints.get(0), endPoint);
    }
    
    
    @Test
    public void testGetBestEndPoint07() {
        System.out.println("getMatchEndPoint 07");
        List<EndPoint> endPoints = new ArrayList<EndPoint>();
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/*", null, null));
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/*", "*", null));
        
        
        EndPoint endPoint = EndPoint.getBestEndPoint(endPoints,"/pepe/juan/pepe.txt" , "adios");
        assertEquals(endPoints.get(1), endPoint);
    }    
    
    @Test(expected = RuntimeException.class)
    public void testGetBestEndPoint08() {
        System.out.println("getMatchEndPoint 08");
        List<EndPoint> endPoints = new ArrayList<EndPoint>();
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/*", null, null));
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/*", null, null));
        
        
        EndPoint.getBestEndPoint(endPoints,"/pepe/juan/pepe.txt" , "adios");
    } 
    
    @Test(expected = RuntimeException.class)
    public void testGetBestEndPoint09() {
        System.out.println("getMatchEndPoint 09");
        List<EndPoint> endPoints = new ArrayList<EndPoint>();
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/*", "*", null));
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/*", "*", null));
        
        
        EndPoint.getBestEndPoint(endPoints,"/pepe/juan/pepe.txt" , "adios");
    }   
    
    @Test
    public void testGetBestEndPoint10() {
        System.out.println("getMatchEndPoint 10");
        List<EndPoint> endPoints = new ArrayList<EndPoint>();
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/*", "adios", null));
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/*", "*", null));
        
        
        EndPoint endPoint = EndPoint.getBestEndPoint(endPoints,"/pepe/juan/pepe.txt" , "adios");
        assertEquals(endPoints.get(0), endPoint);
    }
    
    @Test
    public void testGetBestEndPoint11() {
        System.out.println("getMatchEndPoint 11");
        List<EndPoint> endPoints = new ArrayList<EndPoint>();
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/*", "*", null));
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/*", "adios", null));
        
        
        EndPoint endPoint = EndPoint.getBestEndPoint(endPoints,"/pepe/juan/pepe.txt" , "adios");
        assertEquals(endPoints.get(1), endPoint);
    }   
    
    @Test
    public void testGetBestEndPoint12() {
        System.out.println("getMatchEndPoint 12");
        List<EndPoint> endPoints = new ArrayList<EndPoint>();
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/*", "adios", null));
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/*", null, null));
        
        
        EndPoint endPoint = EndPoint.getBestEndPoint(endPoints,"/pepe/juan/pepe.txt" , "adios");
        assertEquals(endPoints.get(0), endPoint);
    }
    
    @Test
    public void testGetBestEndPoint13() {
        System.out.println("getMatchEndPoint 13");
        List<EndPoint> endPoints = new ArrayList<EndPoint>();
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/*", null, null));
        endPoints.add(EndPoint.createEndPoint("/pepe/juan/*", "adios", null));
        
        
        EndPoint endPoint = EndPoint.getBestEndPoint(endPoints,"/pepe/juan/pepe.txt" , "adios");
        assertEquals(endPoints.get(1), endPoint);
    }      
    
}
