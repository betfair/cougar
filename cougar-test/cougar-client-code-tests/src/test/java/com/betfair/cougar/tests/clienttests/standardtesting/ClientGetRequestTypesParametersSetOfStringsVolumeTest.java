/*
 * Copyright 2013, The Sporting Exchange Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Originally from ClientTests/Transport/StandardTesting/Client_Rescript_Get_RequestTypes_Parameters_SetOfStrings_Volume.xls;
package com.betfair.cougar.tests.clienttests.standardtesting;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.baseline.v2.to.NonMandatoryParamsOperationResponseObject;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientResponseTypeUtils;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Set;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Ensure that when a stringSet with a large volume of entries is passed to cougar via a cougar client the request is sent and the response is handled correctly
 */
public class ClientGetRequestTypesParametersSetOfStringsVolumeTest {
    @Test(dataProvider = "TransportType")
    public void doTest(CougarClientWrapper.TransportType tt) throws Exception {
        // Set up the client to use rescript transport
        CougarClientWrapper cougarClientWrapper1 = CougarClientWrapper.getInstance(tt);
        CougarClientWrapper wrapper = cougarClientWrapper1;
        BaselineSyncClient client = cougarClientWrapper1.getClient();
        ExecutionContext context = cougarClientWrapper1.getCtx();
        // Build set to pass as parameter
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils2 = new CougarClientResponseTypeUtils();
        Set<String> querySet = cougarClientResponseTypeUtils2.buildSet("entry1,entry2,entry3,entry4,entry5,entry6,entry7,entry8,entry9,entry10,entry11,entry12,entry13,entry14,entry15,entry16,entry17,entry18,entry19,entry20,entry21,entry22,entry23,entry24,entry25,entry26,entry27,entry28,entry29,entry30,entry31,entry32,entry33,entry34,entry35,entry36,entry37,entry38,entry39,entry40,entry41,entry42,entry43,entry44,entry45,entry46,entry47,entry48,entry49,entry50,entry51,entry52,entry53,entry54,entry55,entry56,entry57,entry58,entry59,entry60,entry61,entry62,entry63,entry64,entry65,entry66,entry67,entry68,entry69,entry70,entry71,entry72,entry73,entry74,entry75,entry76,entry77,entry78,entry79,entry80,entry81,entry82,entry83,entry84,entry85,entry86,entry87,entry88,entry89,entry90,entry91,entry92,entry93,entry94,entry95,entry96,entry97,entry98,entry99,entry100,entry101,entry102,entry103,entry104,entry105,entry106,entry107,entry108,entry109,entry110,entry111,entry112,entry113,entry114,entry115,entry116,entry117,entry118,entry119,entry120,entry121,entry122,entry123,entry124,entry125,entry126,entry127,entry128,entry129,entry130,entry131,entry132,entry133,entry134,entry135,entry136,entry137,entry138,entry139,entry140,entry141,entry142,entry143,entry144,entry145,entry146,entry147,entry148,entry149,entry150,entry151,entry152,entry153,entry154,entry155,entry156,entry157,entry158,entry159,entry160,entry161,entry162,entry163,entry164,entry165,entry166,entry167,entry168,entry169,entry170,entry171,entry172,entry173,entry174,entry175,entry176,entry177,entry178,entry179,entry180,entry181,entry182,entry183,entry184,entry185,entry186,entry187,entry188,entry189,entry190,entry191,entry192,entry193,entry194,entry195,entry196,entry197,entry198,entry199,entry200,entry201,entry202,entry203,entry204,entry205,entry206,entry207,entry208,entry209,entry210,entry211,entry212,entry213,entry214,entry215,entry216,entry217,entry218,entry219,entry220,entry221,entry222,entry223,entry224,entry225,entry226,entry227,entry228,entry229,entry230,entry231,entry232,entry233,entry234,entry235,entry236,entry237,entry238,entry239,entry240,entry241,entry242,entry243,entry244,entry245,entry246,entry247,entry248,entry249,entry250,entry251,entry252,entry253,entry254,entry255,entry256,entry257,entry258,entry259,entry260,entry261,entry262,entry263,entry264,entry265,entry266,entry267,entry268,entry269,entry270,entry271,entry272,entry273,entry274,entry275,entry276,entry277,entry278,entry279,entry280,entry281,entry282,entry283,entry284,entry285,entry286,entry287,entry288,entry289,entry290,entry291,entry292,entry293,entry294,entry295,entry296,entry297,entry298,entry299,entry300");
        // Build set to pass as parameter
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils3 = new CougarClientResponseTypeUtils();
        Set<String> headerSet = cougarClientResponseTypeUtils3.buildSet("header1,header2");
        // Make call to the method via client and validate the same sets have been echoed in response
        NonMandatoryParamsOperationResponseObject response4 = client.stringSetOperation(context, headerSet, querySet);
        assertEquals("header1,header2", response4.getHeaderParameter());
        assertEquals("entry1,entry10,entry100,entry101,entry102,entry103,entry104,entry105,entry106,entry107,entry108,entry109,entry11,entry110,entry111,entry112,entry113,entry114,entry115,entry116,entry117,entry118,entry119,entry12,entry120,entry121,entry122,entry123,entry124,entry125,entry126,entry127,entry128,entry129,entry13,entry130,entry131,entry132,entry133,entry134,entry135,entry136,entry137,entry138,entry139,entry14,entry140,entry141,entry142,entry143,entry144,entry145,entry146,entry147,entry148,entry149,entry15,entry150,entry151,entry152,entry153,entry154,entry155,entry156,entry157,entry158,entry159,entry16,entry160,entry161,entry162,entry163,entry164,entry165,entry166,entry167,entry168,entry169,entry17,entry170,entry171,entry172,entry173,entry174,entry175,entry176,entry177,entry178,entry179,entry18,entry180,entry181,entry182,entry183,entry184,entry185,entry186,entry187,entry188,entry189,entry19,entry190,entry191,entry192,entry193,entry194,entry195,entry196,entry197,entry198,entry199,entry2,entry20,entry200,entry201,entry202,entry203,entry204,entry205,entry206,entry207,entry208,entry209,entry21,entry210,entry211,entry212,entry213,entry214,entry215,entry216,entry217,entry218,entry219,entry22,entry220,entry221,entry222,entry223,entry224,entry225,entry226,entry227,entry228,entry229,entry23,entry230,entry231,entry232,entry233,entry234,entry235,entry236,entry237,entry238,entry239,entry24,entry240,entry241,entry242,entry243,entry244,entry245,entry246,entry247,entry248,entry249,entry25,entry250,entry251,entry252,entry253,entry254,entry255,entry256,entry257,entry258,entry259,entry26,entry260,entry261,entry262,entry263,entry264,entry265,entry266,entry267,entry268,entry269,entry27,entry270,entry271,entry272,entry273,entry274,entry275,entry276,entry277,entry278,entry279,entry28,entry280,entry281,entry282,entry283,entry284,entry285,entry286,entry287,entry288,entry289,entry29,entry290,entry291,entry292,entry293,entry294,entry295,entry296,entry297,entry298,entry299,entry3,entry30,entry300,entry31,entry32,entry33,entry34,entry35,entry36,entry37,entry38,entry39,entry4,entry40,entry41,entry42,entry43,entry44,entry45,entry46,entry47,entry48,entry49,entry5,entry50,entry51,entry52,entry53,entry54,entry55,entry56,entry57,entry58,entry59,entry6,entry60,entry61,entry62,entry63,entry64,entry65,entry66,entry67,entry68,entry69,entry7,entry70,entry71,entry72,entry73,entry74,entry75,entry76,entry77,entry78,entry79,entry8,entry80,entry81,entry82,entry83,entry84,entry85,entry86,entry87,entry88,entry89,entry9,entry90,entry91,entry92,entry93,entry94,entry95,entry96,entry97,entry98,entry99", response4.getQueryParameter());
    }

    @DataProvider(name="TransportType")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }

}
