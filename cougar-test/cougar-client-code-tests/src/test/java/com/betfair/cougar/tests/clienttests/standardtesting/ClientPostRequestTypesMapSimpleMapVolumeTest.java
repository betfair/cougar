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

// Originally from ClientTests/Transport/StandardTesting/Client_Rescript_Post_RequestTypes_Map_SimpleMap_Volume.xls;
package com.betfair.cougar.tests.clienttests.standardtesting;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.baseline.v2.to.BodyParamSimpleMapObject;
import com.betfair.baseline.v2.to.SimpleMapOperationResponseObject;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientResponseTypeUtils;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Ensure that when a SimpleMap object with a large volume of entries is passed in parameters to cougar via a cougar client, the request is sent and the response is handled correctly
 */
public class ClientPostRequestTypesMapSimpleMapVolumeTest {
    @Test(dataProvider = "TransportType")
    public void doTest(CougarClientWrapper.TransportType tt) throws Exception {
        // Set up the client to use rescript transport
        CougarClientWrapper cougarClientWrapper1 = CougarClientWrapper.getInstance(tt);
        CougarClientWrapper wrapper = cougarClientWrapper1;
        BaselineSyncClient client = cougarClientWrapper1.getClient();
        ExecutionContext context = cougarClientWrapper1.getCtx();
        // Build map with a large volume of entries
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils2 = new CougarClientResponseTypeUtils();
        Map<String, String> simpleMap = cougarClientResponseTypeUtils2.buildMap("1,10,100,101,102,103,104,105,106,107,108,109,11,110,111,112,113,114,115,116,117,118,119,12,120,121,122,123,124,125,126,127,128,129,13,130,131,132,133,134,135,136,137,138,139,14,140,141,142,143,144,145,146,147,148,149,15,150,151,152,153,154,155,156,157,158,159,16,160,161,162,163,164,165,166,167,168,169,17,170,171,172,173,174,175,176,177,178,179,18,180,181,182,183,184,185,186,187,188,189,19,190,191,192,193,194,195,196,197,198,199,2,20,200,201,202,203,204,205,206,207,208,209,21,210,211,212,213,214,215,216,217,218,219,22,220,221,222,223,224,225,226,227,228,229,23,230,231,232,233,234,235,236,237,238,239,24,240,241,242,243,244,245,246,247,248,249,25,250,251,252,253,254,255,256,257,258,259,26,260,261,262,263,264,265,266,267,268,269,27,270,271,272,273,274,275,276,277,278,279,28,280,281,282,283,284,285,286,287,288,289,29,290,291,292,293,294,295,296,297,298,299,3,30,300,301,302,303,304,305,306,307,308,309,31,310,311,312,313,314,315,316,317,318,319,32,320,321,322,323,324,325,326,327,328,329,33,330,331,332,333,334,335,336,337,338,339,34,340,341,342,343,344,345,346,347,348,349,35,350,351,352,353,354,355,356,357,358,359,36,360,361,362,363,364,365,366,367,368,369,37,370,371,372,373,374,375,376,377,378,379,38,380,381,382,383,384,385,386,387,388,389,39,390,391,392,393,394,395,396,397,398,399,4,40,400,401,402,403,404,405,406,407,408,409,41,410,411,412,413,414,415,416,417,418,419,42,420,421,422,423,424,425,426,427,428,429,43,430,431,432,433,434,435,436,437,438,439,44,440,441,442,443,444,445,446,447,448,449,45,450,451,452,453,454,455,456,457,458,459,46,460,461,462,463,464,465,466,467,468,469,47,470,471,472,473,474,475,476,477,478,479,48,480,481,482,483,484,485,486,487,488,489,49,490,491,492,493,494,495,496,497,498,499,5,50,500,51,52,53,54,55,56,57,58,59,6,60,61,62,63,64,65,66,67,68,69,7,70,71,72,73,74,75,76,77,78,79,8,80,81,82,83,84,85,86,87,88,89,9,90,91,92,93,94,95,96,97,98,99", "string 001,string 010,string 100,string 101,string 102,string 103,string 104,string 105,string 106,string 107,string 108,string 109,string 011,string 110,string 111,string 112,string 113,string 114,string 115,string 116,string 117,string 118,string 119,string 012,string 120,string 121,string 122,string 123,string 124,string 125,string 126,string 127,string 128,string 129,string 013,string 130,string 131,string 132,string 133,string 134,string 135,string 136,string 137,string 138,string 139,string 014,string 140,string 141,string 142,string 143,string 144,string 145,string 146,string 147,string 148,string 149,string 015,string 150,string 151,string 152,string 153,string 154,string 155,string 156,string 157,string 158,string 159,string 016,string 160,string 161,string 162,string 163,string 164,string 165,string 166,string 167,string 168,string 169,string 017,string 170,string 171,string 172,string 173,string 174,string 175,string 176,string 177,string 178,string 179,string 018,string 180,string 181,string 182,string 183,string 184,string 185,string 186,string 187,string 188,string 189,string 019,string 190,string 191,string 192,string 193,string 194,string 195,string 196,string 197,string 198,string 199,string 002,string 020,string 200,string 201,string 202,string 203,string 204,string 205,string 206,string 207,string 208,string 209,string 021,string 210,string 211,string 212,string 213,string 214,string 215,string 216,string 217,string 218,string 219,string 022,string 220,string 221,string 222,string 223,string 224,string 225,string 226,string 227,string 228,string 229,string 023,string 230,string 231,string 232,string 233,string 234,string 235,string 236,string 237,string 238,string 239,string 024,string 240,string 241,string 242,string 243,string 244,string 245,string 246,string 247,string 248,string 249,string 025,string 250,string 251,string 252,string 253,string 254,string 255,string 256,string 257,string 258,string 259,string 026,string 260,string 261,string 262,string 263,string 264,string 265,string 266,string 267,string 268,string 269,string 027,string 270,string 271,string 272,string 273,string 274,string 275,string 276,string 277,string 278,string 279,string 028,string 280,string 281,string 282,string 283,string 284,string 285,string 286,string 287,string 288,string 289,string 029,string 290,string 291,string 292,string 293,string 294,string 295,string 296,string 297,string 298,string 299,string 003,string 030,string 300,string 301,string 302,string 303,string 304,string 305,string 306,string 307,string 308,string 309,string 031,string 310,string 311,string 312,string 313,string 314,string 315,string 316,string 317,string 318,string 319,string 032,string 320,string 321,string 322,string 323,string 324,string 325,string 326,string 327,string 328,string 329,string 033,string 330,string 331,string 332,string 333,string 334,string 335,string 336,string 337,string 338,string 339,string 034,string 340,string 341,string 342,string 343,string 344,string 345,string 346,string 347,string 348,string 349,string 035,string 350,string 351,string 352,string 353,string 354,string 355,string 356,string 357,string 358,string 359,string 036,string 360,string 361,string 362,string 363,string 364,string 365,string 366,string 367,string 368,string 369,string 037,string 370,string 371,string 372,string 373,string 374,string 375,string 376,string 377,string 378,string 379,string 038,string 380,string 381,string 382,string 383,string 384,string 385,string 386,string 387,string 388,string 389,string 039,string 390,string 391,string 392,string 393,string 394,string 395,string 396,string 397,string 398,string 399,string 004,string 040,string 400,string 401,string 402,string 403,string 404,string 405,string 406,string 407,string 408,string 409,string 041,string 410,string 411,string 412,string 413,string 414,string 415,string 416,string 417,string 418,string 419,string 042,string 420,string 421,string 422,string 423,string 424,string 425,string 426,string 427,string 428,string 429,string 043,string 430,string 431,string 432,string 433,string 434,string 435,string 436,string 437,string 438,string 439,string 044,string 440,string 441,string 442,string 443,string 444,string 445,string 446,string 447,string 448,string 449,string 045,string 450,string 451,string 452,string 453,string 454,string 455,string 456,string 457,string 458,string 459,string 046,string 460,string 461,string 462,string 463,string 464,string 465,string 466,string 467,string 468,string 469,string 047,string 470,string 471,string 472,string 473,string 474,string 475,string 476,string 477,string 478,string 479,string 048,string 480,string 481,string 482,string 483,string 484,string 485,string 486,string 487,string 488,string 489,string 049,string 490,string 491,string 492,string 493,string 494,string 495,string 496,string 497,string 498,string 499,string 005,string 050,string 500,string 051,string 052,string 053,string 054,string 055,string 056,string 057,string 058,string 059,string 006,string 060,string 061,string 062,string 063,string 064,string 065,string 066,string 067,string 068,string 069,string 007,string 070,string 071,string 072,string 073,string 074,string 075,string 076,string 077,string 078,string 079,string 008,string 080,string 081,string 082,string 083,string 084,string 085,string 086,string 087,string 088,string 089,string 009,string 090,string 091,string 092,string 093,string 094,string 095,string 096,string 097,string 098,string 099,");
        // Create body parameter to be passed
        BodyParamSimpleMapObject bodyParamSimpleMapObject3 = new BodyParamSimpleMapObject();
        bodyParamSimpleMapObject3.setSimpleMap(simpleMap);
        BodyParamSimpleMapObject bodyParam = bodyParamSimpleMapObject3;
        // Make call to the method via client and validate the response is as expected
        SimpleMapOperationResponseObject response5 = client.simpleMapOperation(context, bodyParam);
        assertEquals(simpleMap, response5.getResponseMap());
    }

    @DataProvider(name="TransportType")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }

}
