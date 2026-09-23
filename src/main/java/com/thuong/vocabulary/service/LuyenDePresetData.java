package com.thuong.vocabulary.service;

import com.thuong.vocabulary.dto.luyende.DeThiQ79DTO;

import java.util.HashMap;
import java.util.Map;

public class LuyenDePresetData {

    private static final Map<Integer, DeThiQ79DTO> PRESET_MAP = new HashMap<>();

    static {
        // -------------------------------------------------------------
        // ĐỀ #1: Annual Human Resources Conference (Sunday, Jul. 30)
        // -------------------------------------------------------------
        PRESET_MAP.put(1, DeThiQ79DTO.builder()
                .tieuDe("Annual Human Resources Conference")
                .loaiNoiDung("IMAGE")
                .anhUrl("/images/de-thi/image1.png")
                .tomTatNoiDung("Annual Human Resources Conference<br>Wayne Arena • Sunday, Jul. 30<br>• 9:00 a.m. - 9:40 a.m.: Lecture: Recruiting Online (Reza Jones)<br>• 9:40 a.m. - 10:30 a.m.: Speech: Company Benefits and Legal Accountability (Sun Woo Nam)<br>• 10:30 a.m. - 11:50 a.m.: Lecture: Training new employees (Joe Leigh)<br>• 12:00 p.m. - 1:30 p.m.: Lunch (included in the registration fee)<br>• 1:30 p.m. - 2:30 p.m.: Workshop: Finding Perfect Careers (Micah Villi)<br>• 2:30 p.m. - 4:00 p.m.: Discussion: Interviewing Online (Astrid Thomson)")
                .vanBanThongTin("Annual Human Resources Conference\nWayne Arena - Sunday, Jul. 30\n9:00 a.m. - 9:40 a.m. | Lecture: Recruiting Online | Reza Jones\n9:40 a.m. - 10:30 a.m. | Speech: Company Benefits and Legal Accountability | Sun Woo Nam\n10:30 a.m. - 11:50 a.m. | Lecture: Training new employees | Joe Leigh\n12:00 p.m. - 1:30 p.m. | Lunch (included in the registration fee)\n1:30 p.m. - 2:30 p.m. | Workshop: Finding Perfect Careers | Micah Villi\n2:30 p.m. - 4:00 p.m. | Discussion: Interviewing Online | Astrid Thomson")
                .tinhHuong("Hello, I'm planning to attend the Annual Human Resources Conference on Sunday, July 30th, but I don't have all the details. Could you please answer a few questions about the schedule?")
                .cauHoi1("What time does the conference start, and who is giving the first lecture?")
                .thoiGianCau1(15)
                .goiYCau1("Sure. The conference starts at 9:00 a.m. with a lecture on Recruiting Online, given by Reza Jones.")
                .cauHoi2("I heard that lunch is not included and we have to pay extra for it. Is that right?")
                .thoiGianCau2(15)
                .goiYCau2("Actually, that's not correct. Lunch is included in the registration fee and will be held from 12:00 p.m. to 1:30 p.m.")
                .cauHoi3("Could you tell me about all the sessions scheduled in the afternoon after lunch?")
                .thoiGianCau3(30)
                .goiYCau3("Certainly, there are two afternoon sessions. First, from 1:30 p.m. to 2:30 p.m., there is a workshop on Finding Perfect Careers led by Micah Villi. Second, from 2:30 p.m. to 4:00 p.m., there is a discussion on Interviewing Online led by Astrid Thomson.")
                .nguonGoc("DE_MAU")
                .deMauId(1)
                .build());

        // -------------------------------------------------------------
        // ĐỀ #2: Future of Education and Careers Seminar (Jan. 2)
        // -------------------------------------------------------------
        PRESET_MAP.put(2, DeThiQ79DTO.builder()
                .tieuDe("Future of Education and Careers Seminar")
                .loaiNoiDung("IMAGE")
                .anhUrl("/images/de-thi/image2.png")
                .tomTatNoiDung("Future of Education and Careers Seminar<br>Jan. 2, Joint Hotel, Las Vegas 235 Smith St.<br>• 05:00 p.m.: Keynote Speech: Introduction to Smart Education for Kids<br>• 05:15 p.m.: Presentation 1 [Essential Academic Education] Margaret Richie, Chairman of SLT Leadership Corporation<br>• 06:00 p.m.: Group Discussion: How to Support International Students<br>• 07:15 p.m.: Dinner Buffet [8th floor, Newton Hall]<br>• 08:30 p.m.: Presentation 2 [Make Seed Money for Careers] Ray Adelman, Investment Director of BV Bank<br>• 09:15 p.m.: Questions & Answers<br>• 09:40 p.m.: Informal Networking Event (until midnight, the Gold Bar)")
                .vanBanThongTin("Future of Education and Careers Seminar\nJan. 2, Joint Hotel, Las Vegas 235 Smith St.\n05:00 p.m. | Keynote Speech: Introduction to Smart Education for Kids\n05:15 p.m. | Presentation 1 [Essential Academic Education] Margaret Richie, Chairman of SLT Leadership Corporation\n06:00 p.m. | Group Discussion: How to Support International Students\n07:15 p.m. | Dinner Buffet [8th floor, Newton Hall]\n08:30 p.m. | Presentation 2 [Make Seed Money for Careers] Ray Adelman, Investment Director of BV Bank\n09:15 p.m. | Questions & Answers\n09:40 p.m. | Informal Networking Event (until midnight, the Gold Bar)")
                .tinhHuong("Hi, I'm calling about the Future of Education and Careers Seminar on January 2nd. Could you give me some information about the program?")
                .cauHoi1("Where will the seminar take place and what time does it begin?")
                .thoiGianCau1(15)
                .goiYCau1("The seminar will take place at Joint Hotel, 235 Smith Street in Las Vegas, and it begins at 5:00 p.m. with a keynote speech.")
                .cauHoi2("I believe Margaret Richie will give a presentation on making seed money for careers. Is that correct?")
                .thoiGianCau2(15)
                .goiYCau2("Actually, that's not correct. Margaret Richie will give Presentation 1 on Essential Academic Education at 5:15 p.m., while Ray Adelman will present on Make Seed Money for Careers at 8:30 p.m.")
                .cauHoi3("Could you please tell me about all the presentations scheduled for the seminar?")
                .thoiGianCau3(30)
                .goiYCau3("Sure, there are two presentations. First, at 5:15 p.m., Margaret Richie from SLT Leadership Corporation will deliver Presentation 1 on Essential Academic Education. Second, at 8:30 p.m., Ray Adelman from BV Bank will deliver Presentation 2 on Make Seed Money for Careers.")
                .nguonGoc("DE_MAU")
                .deMauId(2)
                .build());

        // -------------------------------------------------------------
        // ĐỀ #3: Resume: Murray O'Brien (Senior Landscape Architect)
        // -------------------------------------------------------------
        PRESET_MAP.put(3, DeThiQ79DTO.builder()
                .tieuDe("Resume: Murray O'Brien")
                .loaiNoiDung("IMAGE")
                .anhUrl("/images/de-thi/image3.png")
                .tomTatNoiDung("Murray O'Brien<br>75 Woodland Street, Holly Bay, Melbourne 23548<br>Phone: (826) 526-8531 / Email: murray.obrien@cmail.com<br>• Position sought: Senior Landscape Architect (Holly Bay area)<br>• Education:<br>  - Bachelor's degree in Landscape Planning: Melbourne College (2011)<br>  - High School diploma: Gold Coast High School (2007)<br>• Work Experience:<br>  - Landscape Architect: Coast Landscaping (2014-present)<br>  - Landscape Gardener: Good Landscaping (2011-2014)<br>• Skills:<br>  - Japanese (fluent), Spanish (conversational)<br>  - Proficient in all major landscape design programs")
                .vanBanThongTin("Murray O'Brien\n75 Woodland Street, Holly Bay, Melbourne 23548\nPhone: (826) 526-8531 / Email: murray.obrien@cmail.com\nPosition sought: Senior Landscape Architect (Holly Bay area)\nEducation:\n- Bachelor's degree in Landscape Planning: Melbourne College (2011)\n- High School diploma: Gold Coast High School (2007)\nWork Experience:\n- Landscape Architect: Coast Landscaping (2014-present)\n- Landscape Gardener: Good Landscaping (2011-2014)\nSkills:\n- Japanese (fluent), Spanish (conversational)\n- Proficient in all major landscape design programs")
                .tinhHuong("Hello, this is the hiring manager at Green Space Design. I'm reviewing Murray O'Brien's resume for the senior landscape architect position, and I have a few questions.")
                .cauHoi1("Which college did Mr. O'Brien graduate from, and what degree does he hold?")
                .thoiGianCau1(15)
                .goiYCau1("Mr. O'Brien graduated from Melbourne College in 2011 with a Bachelor's degree in Landscape Planning.")
                .cauHoi2("He has worked at Coast Landscaping since 2011, right?")
                .thoiGianCau2(15)
                .goiYCau2("Actually, that's not correct. He worked as a Landscape Gardener at Good Landscaping from 2011 to 2014, and has been with Coast Landscaping since 2014.")
                .cauHoi3("Could you please provide details about all of Mr. O'Brien's work experience in landscaping?")
                .thoiGianCau3(30)
                .goiYCau3("Certainly. He has two work experiences. First, from 2011 to 2014, he worked as a Landscape Gardener at Good Landscaping. Second, from 2014 to the present, he has been working as a Landscape Architect at Coast Landscaping.")
                .nguonGoc("DE_MAU")
                .deMauId(3)
                .build());

        // -------------------------------------------------------------
        // ĐỀ #4: Anna Vales' Flower Shop Delivery
        // -------------------------------------------------------------
        PRESET_MAP.put(4, DeThiQ79DTO.builder()
                .tieuDe("Anna Vales' Flower Shop Delivery")
                .loaiNoiDung("IMAGE")
                .anhUrl("/images/de-thi/image4.png")
                .tomTatNoiDung("Anna Vales' Flower Shop (Founded 1973)<br>13th St., Summerville, New Orleans • (716) 220-3852<br>• Order made: July 5th<br>• Destination: Ballroom, Lamington Hotel (Rear Entrance), 34th St.<br>• Delivery date: July 8th<br>• Delivery No.: 389-SC<br>• Delivery detail:<br>  - Sunflowers: twenty<br>  - Marigolds: two dozen<br>  - Cosmos: thirteen (white), seven (pink)<br>• Delivery charge: $325 (to be paid upon delivery)")
                .vanBanThongTin("Anna Vales' Flower Shop (Founded 1973)\n13th St., Summerville, New Orleans | (716) 220-3852\nOrder made: July 5th\nDestination: Ballroom, Lamington Hotel (Rear Entrance), 34th St.\nDelivery date: July 8th\nDelivery No.: 389-SC\nDelivery detail:\n- Sunflowers: twenty\n- Marigolds: two dozen\n- Cosmos: thirteen (white), seven (pink)\nDelivery charge: $325 (to be paid upon delivery)")
                .tinhHuong("Hello, this is Ms. Rivera from Lamington Hotel. I'm calling to verify the details for the flower delivery scheduled for our ballroom event. Could you please answer a few questions for me?")
                .cauHoi1("Could you tell me when the flowers will be delivered and where they should be brought to?")
                .thoiGianCau1(15)
                .goiYCau1("Sure. The delivery is scheduled for July 8th, and it should be delivered to the Ballroom at Lamington Hotel via the rear entrance on 34th Street.")
                .cauHoi2("I believe the delivery charge has already been paid in advance. Is that right?")
                .thoiGianCau2(15)
                .goiYCau2("Actually, that's not correct. The delivery charge is $325, and it is to be paid upon delivery.")
                .cauHoi3("Could you please give me the complete details of all the flowers included in this order?")
                .thoiGianCau3(30)
                .goiYCau3("Certainly. There are three types of flowers in this order. First, there are twenty sunflowers. Second, there are two dozen marigolds. Finally, there are twenty cosmos, consisting of thirteen white ones and seven pink ones.")
                .nguonGoc("DE_MAU")
                .deMauId(4)
                .build());

        // -------------------------------------------------------------
        // ĐỀ #5: Sunrise Pharmaceutical Quarterly Meeting (Monday, April 28th)
        // -------------------------------------------------------------
        PRESET_MAP.put(5, DeThiQ79DTO.builder()
                .tieuDe("Sunrise Pharmaceutical Quarterly Meeting")
                .loaiNoiDung("IMAGE")
                .anhUrl("/images/de-thi/image5.png")
                .tomTatNoiDung("Sunrise Pharmaceutical Company<br>Quarterly Manager's Meeting<br>Monday, April 28th Annex Building 3rd Floor Conference Room<br>• 9:30 a.m. - 10:00 a.m.: Remarks by James Toyota, Company President<br>• 10:00 a.m. - 10:45 a.m.: Discussion on Company Direction & Goals<br>• 11:30 a.m. - Noon: Information about Planned Construction (postponed)<br>• Noon - 1:30 p.m.: Lunch (corporate dining room)<br>• 1:30 p.m. - 3:30 p.m.: Managers' Quarterly Reports (Upcoming Projects - Frederic Creeks; New Market Possibilities - Maria Lawson)<br>• 3:30 p.m. - 4:30 p.m.: Q & A")
                .vanBanThongTin("Sunrise Pharmaceutical Company\nQuarterly Manager's Meeting\nMonday, April 28th Annex Building 3rd Floor Conference Room\n9:30 a.m. - 10:00 a.m. | Remarks by James Toyota, Company President\n10:00 a.m. - 10:45 a.m. | Discussion on Company Direction & Goals\n11:30 a.m. - Noon | Information about Planned Construction (postponed)\nNoon - 1:30 p.m. | Lunch (corporate dining room)\n1:30 p.m. - 3:30 p.m. | Managers' Quarterly Reports:\n  - Upcoming Projects (Frederic Creeks, development manager)\n  - New Market Possibilities (Maria Lawson, marketing manager)\n3:30 p.m. - 4:30 p.m. | Q & A")
                .tinhHuong("Hi, I'm calling to check the schedule for the quarterly manager's meeting next Monday. Could you please answer some questions for me?")
                .cauHoi1("Where will the meeting be held, and what is scheduled for the first 30 minutes?")
                .thoiGianCau1(15)
                .goiYCau1("The meeting will be held in the Annex Building 3rd Floor Conference Room, and from 9:30 to 10:00 a.m., there will be remarks by Company President James Toyota.")
                .cauHoi2("I heard that the session about planned construction will take place before lunch. Is that still on schedule?")
                .thoiGianCau2(15)
                .goiYCau2("Actually, that's not correct. The information session about planned construction has been postponed, so it will not be held.")
                .cauHoi3("Could you tell me about the managers' quarterly reports scheduled in the afternoon?")
                .thoiGianCau3(30)
                .goiYCau3("Sure, there are two quarterly reports between 1:30 and 3:30 p.m. First, Frederic Creeks, the development manager, will report on Upcoming Projects. Second, Maria Lawson, the marketing manager, will present on New Market Possibilities.")
                .nguonGoc("DE_MAU")
                .deMauId(5)
                .build());

        // -------------------------------------------------------------
        // ĐỀ #6: Seminars for You and Your Family (Union Bank)
        // -------------------------------------------------------------
        PRESET_MAP.put(6, DeThiQ79DTO.builder()
                .tieuDe("Seminars for You and Your Family")
                .loaiNoiDung("IMAGE")
                .anhUrl("/images/de-thi/image6.png")
                .tomTatNoiDung("Seminars for you and your family 9 a.m. to 12 p.m.<br>Conference Room 1, Union Bank of America • Free! Open to the public<br>• June 8: How to provide kids with pocket money (Sam Noel) [Rescheduled from June 5]<br>• June 14: Strategies for increasing bank interest (Sam Noel)<br>• July 10: Teaching your children to spend wisely (Peter Kendall)<br>• July 19: Saving for your children's education (Peter Kendall)<br>• August 6: Preparation for your retirement (Sally Homes)")
                .vanBanThongTin("Seminars for you and your family 9 a.m. to 12 p.m.\nConference Room 1, Union Bank of America\nFree! Open to the public\nJune 8 | How to provide kids with pocket money | Sam Noel (Rescheduled from June 5)\nJune 14 | Strategies for increasing bank interest | Sam Noel\nJuly 10 | Teaching your children to spend wisely | Peter Kendall\nJuly 19 | Saving for your children's education | Peter Kendall\nAugust 6 | Preparation for your retirement | Sally Homes")
                .tinhHuong("Hello, I saw the flyer for the family financial seminars at Union Bank of America, and I have a few questions about the dates and speakers.")
                .cauHoi1("What time do the seminars usually take place, and how much is the admission fee?")
                .thoiGianCau1(15)
                .goiYCau1("All seminars take place from 9:00 a.m. to 12:00 p.m., and they are completely free and open to the public.")
                .cauHoi2("I'm interested in the seminar on how to provide kids with pocket money. Is it on June 5th?")
                .thoiGianCau2(15)
                .goiYCau2("Actually, the seminar on June 5th was rescheduled; it will now take place on June 8th, led by Sam Noel.")
                .cauHoi3("Could you please give me details on all the seminars presented by Peter Kendall?")
                .thoiGianCau3(30)
                .goiYCau3("Certainly. Peter Kendall is leading two seminars. First, on July 10th, he will present 'Teaching your children to spend wisely'. Second, on July 19th, he will present 'Saving for your children's education'. Both sessions run from 9:00 a.m. to 12:00 p.m.")
                .nguonGoc("DE_MAU")
                .deMauId(6)
                .build());

        // -------------------------------------------------------------
        // ĐỀ #7: Fall International Culture Events (September)
        // -------------------------------------------------------------
        PRESET_MAP.put(7, DeThiQ79DTO.builder()
                .tieuDe("Fall International Culture Events")
                .loaiNoiDung("IMAGE")
                .anhUrl("/images/de-thi/image7.png")
                .tomTatNoiDung("Fall Event - September: International Culture Events<br>• Sep. 4th: 9:00 a.m. - 3:30 p.m. | Underground Band Music Festival ($15)<br>• Sep. 10th: 9:30 a.m. - 10:30 a.m. | Asian Food Fair ($15)<br>• Sep. 16th: 10:30 a.m. - 11:40 p.m. | Chinese Tai Chi Performance ($20)<br>• Sep. 20th: Noon | Italian Food Festival ($8)<br>• Sep. 24th: 1:30 p.m. - 2:30 p.m. | Outdoor Film Festival (Free admission)<br>• Sep. 29th: 2:30 p.m. - 4:00 p.m. | Live Music Concert & Photo Wall: Chris White ($30)<br>• Sep. 30th: 4:00 p.m. - 4:45 p.m. | Japanese Traditional Dance: The Mikado (Free admission)")
                .vanBanThongTin("Fall Event - September: International Culture Events\nSep. 4th | 9:00 a.m. - 3:30 p.m. | Underground Band Music Festival | $15\nSep. 10th | 9:30 a.m. - 10:30 a.m. | Asian Food Fair | $15\nSep. 16th | 10:30 a.m. - 11:40 p.m. | Chinese Tai Chi Performance | $20\nSep. 20th | Noon | Italian Food Festival | $8\nSep. 24th | 1:30 p.m. - 2:30 p.m. | Outdoor Film Festival | Free admission\nSep. 29th | 2:30 p.m. - 4:00 p.m. | Live Music Concert & Photo Wall: Chris White | $30\nSep. 30th | 4:00 p.m. - 4:45 p.m. | Japanese Traditional Dance: The Mikado | Free admission")
                .tinhHuong("Hello, I'm interested in attending some events during the International Culture month in September, and I'd like to ask a few questions.")
                .cauHoi1("What is the first event of the month, and how much does it cost?")
                .thoiGianCau1(15)
                .goiYCau1("The first event is the Underground Band Music Festival on September 4th from 9:00 a.m. to 3:30 p.m., and admission is $15.")
                .cauHoi2("I heard the Italian Food Festival on September 20th is free of charge. Is that true?")
                .thoiGianCau2(15)
                .goiYCau2("Actually, that's not correct. Admission to the Italian Food Festival on September 20th at noon costs $8.")
                .cauHoi3("Could you tell me about all the events that have free admission?")
                .thoiGianCau3(30)
                .goiYCau3("Sure, there are two free events. First, on September 24th from 1:30 p.m. to 2:30 p.m., there is the Outdoor Film Festival. Second, on September 30th from 4:00 p.m. to 4:45 p.m., there is a Japanese Traditional Dance performance called The Mikado.")
                .nguonGoc("DE_MAU")
                .deMauId(7)
                .build());

        // -------------------------------------------------------------
        // ĐỀ #8: Henkel Film Festival (10. 10~15)
        // -------------------------------------------------------------
        PRESET_MAP.put(8, DeThiQ79DTO.builder()
                .tieuDe("Henkel Film Festival")
                .loaiNoiDung("IMAGE")
                .anhUrl("/images/de-thi/image8.png")
                .tomTatNoiDung("Henkel Film Festival • Dates: 10. 10~15<br>• Spy 2 (Comedy) | Cinema 1 | Show time: 9:20 / 10:30 / 13:20 / 16:40<br>• Controller (Action) | Cinema 3 | Show time: 10:10 / 11:40 / 14:40 / 20:10<br>• Holidays in Paris (Romantic) | Cinema 2 | Show time: *12:20 / 15:40 / 22:40 (*Director's review)<br>• Gone With the Wind (Romantic) | Cinema 1 | Show time: *10:40 / 13:50 / 19:30 / 22:45 (*Director's review)")
                .vanBanThongTin("Henkel Film Festival - Dates: 10. 10~15\nSpy 2 | Cinema 1 | Comedy | 9:20 / 10:30 / 13:20 / 16:40\nController | Cinema 3 | Action | 10:10 / 11:40 / 14:40 / 20:10\nHolidays in Paris | Cinema 2 | Romantic | *12:20 / 15:40 / 22:40 (*Director's review)\nGone With the Wind | Cinema 1 | Romantic | *10:40 / 13:50 / 19:30 / 22:45 (*Director's review)")
                .tinhHuong("Hi, I'm planning to watch some movies at the Henkel Film Festival next week, and I'd like to ask about the movie schedule.")
                .cauHoi1("What genre is the movie Controller, and which cinema is it showing at?")
                .thoiGianCau1(15)
                .goiYCau1("The movie Controller is an action film, and it will be shown in Cinema 3.")
                .cauHoi2("I'd like to see Spy 2 in the evening. Is there a screening after 8:00 p.m.?")
                .thoiGianCau2(15)
                .goiYCau2("Actually, that's not correct. The latest screening for Spy 2 is at 4:40 p.m. (16:40), so there is no evening show after 8:00 p.m.")
                .cauHoi3("Could you tell me about all the romantic movies that include a director's review?")
                .thoiGianCau3(30)
                .goiYCau3("Certainly. There are two romantic movies with a director's review. First, 'Holidays in Paris' in Cinema 2 has screenings at 12:20, 15:40, and 22:40. Second, 'Gone With the Wind' in Cinema 1 has screenings at 10:40, 13:50, 19:30, and 22:45.")
                .nguonGoc("DE_MAU")
                .deMauId(8)
                .build());

        // -------------------------------------------------------------
        // ĐỀ #9: Bristol Co. Annual Conference Meeting (Aug 30, 2017)
        // -------------------------------------------------------------
        PRESET_MAP.put(9, DeThiQ79DTO.builder()
                .tieuDe("Bristol Co. Annual Conference Meeting")
                .loaiNoiDung("IMAGE")
                .anhUrl("/images/de-thi/image9.png")
                .tomTatNoiDung("Bristol Co. Annual Conference Meeting<br>Aug 30, 2017 • Baymont Hotel (Conference Room 302)<br>• 10:00 a.m.: Opening Address (Olivia Powell - HR Dept.)<br>• 10:30 a.m.: 2018 Business Plans (Leo Quinn - Marketing Dept.)<br>• 12:00 p.m.: Lunch (Grace's Catering Service - the East Ballroom)<br>• 1:00 p.m.: Annual Marketing Report (Josh McCarthy - Marketing Dept.)<br>• 2:40 p.m.: Presentation: A Perfect Mix of Merchandising and Inventory Control (Jessica Bennett - Merchandising Dept.)<br>• 4:00 p.m.: Marketing Plans in 3 years (Leo Quinn - Marketing Dept.)<br>• 5:00 p.m.: Sales Tips: Question & Answer (Melisa Yi - Sales Dept.)")
                .vanBanThongTin("Bristol Co. Annual Conference Meeting\nAug 30, 2017 | Baymont Hotel (Conference Room 302)\n10:00 a.m. | Opening Address (Olivia Powell - HR Dept.)\n10:30 a.m. | 2018 Business Plans (Leo Quinn - Marketing Dept.)\n12:00 p.m. | Lunch (Grace's Catering Service - the East Ballroom)\n1:00 p.m. | Annual Marketing Report (Josh McCarthy - Marketing Dept.)\n2:40 p.m. | Presentation: A Perfect Mix of Merchandising and Inventory Control (Jessica Bennett - Merchandising Dept.)\n4:00 p.m. | Marketing Plans in 3 years (Leo Quinn - Marketing Dept.)\n5:00 p.m. | Sales Tips: Question & Answer (Melisa Yi - Sales Dept.)")
                .tinhHuong("Hello, this is Sarah from the accounting department. I'm checking the schedule for our annual conference meeting on August 30th, and I have a few questions.")
                .cauHoi1("Where will the conference meeting take place, and what is the first presentation?")
                .thoiGianCau1(15)
                .goiYCau1("The meeting will take place in Conference Room 302 at Baymont Hotel, starting at 10:00 a.m. with an opening address by Olivia Powell from the HR department.")
                .cauHoi2("Will lunch be served in Conference Room 302?")
                .thoiGianCau2(15)
                .goiYCau2("Actually, that's not correct. Lunch will be provided by Grace's Catering Service in the East Ballroom at 12:00 p.m.")
                .cauHoi3("Could you tell me about all the presentations given by Leo Quinn from the Marketing Department?")
                .thoiGianCau3(30)
                .goiYCau3("Sure, Leo Quinn is giving two presentations. First, at 10:30 a.m., he will present the 2018 Business Plans. Second, at 4:00 p.m., he will present the Marketing Plans in 3 years.")
                .nguonGoc("DE_MAU")
                .deMauId(9)
                .build());

        // -------------------------------------------------------------
        // ĐỀ #10: New Employee Orientation (June 3)
        // -------------------------------------------------------------
        PRESET_MAP.put(10, DeThiQ79DTO.builder()
                .tieuDe("New Employee Orientation")
                .loaiNoiDung("IMAGE")
                .anhUrl("/images/de-thi/image10.png")
                .tomTatNoiDung("New Employee Orientation - June 3 (Tues)<br>Arrival: Conference Room B, Chrystal Building<br>• 9:00 a.m. - 10:00 a.m.: Welcome Speech: Creating Positive Change, Amelia Coleman (HR)<br>• 10:00 a.m. - 10:40 a.m.: Tour of Offices, Terry Lane<br>• 10:40 a.m. - 11:15 a.m.: Break* (*Coffee and muffins available, coffee cart, building lobby)<br>• 11:15 a.m. - 11:50 a.m.: Introduction of Team Members, Daniel White, Vice President<br>• 12:00 p.m. - 1:00 p.m.: Lunch (cafeteria)<br>• 1:00 p.m. - 2:00 p.m.: Review of Training Materials, Terry Lane")
                .vanBanThongTin("New Employee Orientation - June 3 (Tues)\nArrival: Conference Room B, Chrystal Building\n9:00 a.m. - 10:00 a.m. | Welcome Speech: Creating Positive Change, Amelia Coleman (HR)\n10:00 a.m. - 10:40 a.m. | Tour of Offices, Terry Lane\n10:40 a.m. - 11:15 a.m. | Break* (*Coffee and muffins available, coffee cart, building lobby)\n11:15 a.m. - 11:50 a.m. | Introduction of Team Members, Daniel White, Vice President\n12:00 p.m. - 1:00 p.m. | Lunch (cafeteria)\n1:00 p.m. - 2:00 p.m. | Review of Training Materials, Terry Lane")
                .tinhHuong("Hello, I'm a new hire attending the orientation next Tuesday, June 3rd. Could you please give me some information about the schedule?")
                .cauHoi1("Where do we meet for the orientation, and what is the first session?")
                .thoiGianCau1(15)
                .goiYCau1("You should arrive at Conference Room B in the Chrystal Building at 9:00 a.m. for a welcome speech on Creating Positive Change by Amelia Coleman.")
                .cauHoi2("I heard that lunch will be served in Conference Room B. Is that correct?")
                .thoiGianCau2(15)
                .goiYCau2("Actually, that's not correct. Lunch will take place in the cafeteria from 12:00 p.m. to 1:00 p.m.")
                .cauHoi3("Could you tell me about all the sessions led by Terry Lane?")
                .thoiGianCau3(30)
                .goiYCau3("Certainly. Terry Lane is leading two sessions. First, from 10:00 a.m. to 10:40 a.m., there is a Tour of Offices. Second, from 1:00 p.m. to 2:00 p.m., there is a Review of Training Materials.")
                .nguonGoc("DE_MAU")
                .deMauId(10)
                .build());

        // -------------------------------------------------------------
        // ĐỀ #11: Resume: Bruce Geller (HR Manager)
        // -------------------------------------------------------------
        PRESET_MAP.put(11, DeThiQ79DTO.builder()
                .tieuDe("Resume: Bruce Geller")
                .loaiNoiDung("IMAGE")
                .anhUrl("/images/de-thi/image11.png")
                .tomTatNoiDung("Bruce Geller • Phone: 0411-553-711 • Email: sirbine@novita.com<br>• Desired position: Human Resources Manager<br>• Preferred branch: Friendship Family Center<br>• Experience:<br>  - Human Resources Specialist, Appletree Hospital (2011 - Present): Redesigned employee training program; Managed recruiting new employees<br>  - Recruiter, University Hospital (2008 - 2011): Recruited and interviewed new employees<br>• Education:<br>  - Fairbanks University, Master's Degree (Business Administration) 2008<br>  - Dale University, Bachelor's Degree (Marketing) 2006")
                .vanBanThongTin("Bruce Geller | 0411-553-711 | sirbine@novita.com\nDesired position: Human Resources Manager\nPreferred branch: Friendship Family Center\nExperience:\n- Human Resources Specialist, Appletree Hospital (2011 - Present)\n  * Redesigned employee training program\n  * Managed recruiting new employees\n- Recruiter, University Hospital (2008 - 2011)\n  * Recruited and interviewed new employees\nEducation:\n- Fairbanks University, Master's Degree (Business Administration) 2008\n- Dale University, Bachelor's Degree (Marketing) 2006")
                .tinhHuong("Hello, I'm calling from the HR recruitment committee. I'm looking at Bruce Geller's resume for the Human Resources Manager opening, and I'd like to verify a few details.")
                .cauHoi1("What position is Mr. Geller applying for, and which branch does he prefer?")
                .thoiGianCau1(15)
                .goiYCau1("Mr. Geller is seeking the position of Human Resources Manager, and his preferred branch is the Friendship Family Center.")
                .cauHoi2("Did Mr. Geller earn his Master's degree from Dale University?")
                .thoiGianCau2(15)
                .goiYCau2("Actually, that's not correct. He earned his Master's degree in Business Administration from Fairbanks University in 2008, and his Bachelor's degree from Dale University in 2006.")
                .cauHoi3("Could you please describe Mr. Geller's employment history in the HR field?")
                .thoiGianCau3(30)
                .goiYCau3("Sure. First, from 2008 to 2011, he worked as a Recruiter at University Hospital. Second, from 2011 to the present, he has been a Human Resources Specialist at Appletree Hospital, where he redesigns training programs and manages recruiting.")
                .nguonGoc("DE_MAU")
                .deMauId(11)
                .build());

        // -------------------------------------------------------------
        // ĐỀ #12: Magnificent Moment Event Planner (Mandy Cooper)
        // -------------------------------------------------------------
        PRESET_MAP.put(12, DeThiQ79DTO.builder()
                .tieuDe("Magnificent Moment Event Planner")
                .loaiNoiDung("IMAGE")
                .anhUrl("/images/de-thi/image12.png")
                .tomTatNoiDung("Magnificent Moment Event Planner - Daily agenda<br>Mandy Cooper, May 23<br>• 8:30 ~ 9:30: Meeting: Haley Taylor, Springhill's banquet manager<br>• 10:30 ~ 11:00: Call Rachel Green (discuss wedding schedule)<br>• 11:00 ~ noon: Meeting: Olivia Nelson - Graduation party<br>• Noon ~ 1:00: Lunch - Beth Bryant, Event photographer<br>• 1:00 ~ 3:00: Meeting: Andrew Simmons, Pure Kitchen Catering (discuss the menu's pricing)<br>• 3:00 ~ 4:30: Visit: Abigail's Flower Creations (pick up decoration samples and catalogues)")
                .vanBanThongTin("Magnificent Moment Event Planner - Daily agenda\nMandy Cooper, May 23\n8:30 ~ 9:30 | Meeting: Haley Taylor, Springhill's banquet manager\n10:30 ~ 11:00 | Call Rachel Green (discuss wedding schedule)\n11:00 ~ noon | Meeting: Olivia Nelson - Graduation party\nNoon ~ 1:00 | Lunch - Beth Bryant, Event photographer\n1:00 ~ 3:00 | Meeting: Andrew Simmons, Pure Kitchen Catering (discuss the menu's pricing)\n3:00 ~ 4:30 | Visit: Abigail's Flower Creations (pick up decoration samples and catalogues)")
                .tinhHuong("Hi Mandy, this is your assistant calling to confirm your daily agenda for May 23rd. Could I go over a few appointments with you?")
                .cauHoi1("What is your first appointment on May 23rd, and what time does it start?")
                .thoiGianCau1(15)
                .goiYCau1("My first appointment is a meeting with Haley Taylor, Springhill's banquet manager, from 8:30 to 9:30 a.m.")
                .cauHoi2("Are you having lunch with Andrew Simmons at noon?")
                .thoiGianCau2(15)
                .goiYCau2("Actually, that's not correct. At noon, I am having lunch with Beth Bryant, the event photographer. My meeting with Andrew Simmons is from 1:00 to 3:00 p.m.")
                .cauHoi3("Could you tell me about all the meetings or appointments scheduled after 1:00 p.m.?")
                .thoiGianCau3(30)
                .goiYCau3("Certainly. After 1:00 p.m., I have two appointments. First, from 1:00 to 3:00 p.m., I have a meeting with Andrew Simmons from Pure Kitchen Catering. Second, from 3:00 to 4:30 p.m., I will visit Abigail's Flower Creations to pick up decoration samples and catalogues.")
                .nguonGoc("DE_MAU")
                .deMauId(12)
                .build());

        // -------------------------------------------------------------
        // ĐỀ #13: High Elevation Rock Festival Tours (Slide 39)
        // -------------------------------------------------------------
        PRESET_MAP.put(13, DeThiQ79DTO.builder()
                .tieuDe("High Elevation Rock Festival Tours")
                .loaiNoiDung("IMAGE")
                .anhUrl("/images/de-thi/image13.png")
                .tomTatNoiDung("High Elevation Rock Festival Tours - Date: March 24th - April 14th<br>• Saturday, March 24th: New York City Tower Dent Hall - Sold out<br>• Saturday, March 31st: Boston Nordic Hall - $22<br>• Friday, April 6th: Richmond Marine Park - Sold out<br>• Saturday, April 14th: New York City Zeppelin Hall - $27<br>Ticket Purchase: online or at the gate. Purchase ticket online and get a $5 extra discount.")
                .vanBanThongTin("High Elevation Rock Festival Tours - Date: March 24th - April 14th\nSaturday, March 24th | New York City Tower Dent Hall - Sold out\nSaturday, March 31st | Boston Nordic Hall - $22\nFriday, April 6th | Richmond Marine Park - Sold out\nSaturday, April 14th | New York City Zeppelin Hall - $27\nTicket Purchase: online or at the gate. Purchase ticket online and get a $5 extra discount.")
                .tinhHuong("Hi, I'm calling for some information about the High Elevation Rock Festival Tours. Could you answer a few questions for me?")
                .cauHoi1("When and where will the High Elevation Rock Festival Tour take place in Boston?")
                .thoiGianCau1(15)
                .goiYCau1("The tour in Boston will take place on Saturday, March 31st at Boston Nordic Hall.")
                .cauHoi2("I'd like to attend the tour in New York City. Could you tell me which dates are available and how much the tickets cost?")
                .thoiGianCau2(15)
                .goiYCau2("There are two dates in New York City. The first tour on Saturday, March 24th at Tower Dent Hall is sold out. However, the tour on Saturday, April 14th at Zeppelin Hall is available for $27.")
                .cauHoi3("I'm planning to buy a ticket for the Boston tour. How much does it cost, and is there any way I can get a discount?")
                .thoiGianCau3(30)
                .goiYCau3("The ticket for the Boston tour costs $22. However, if you purchase your ticket online, you can get a $5 extra discount, so it will cost only $17.")
                .nguonGoc("DE_MAU")
                .deMauId(13)
                .build());

        // -------------------------------------------------------------
        // ĐỀ #14: Vista City Annual Festival (June 21 at Riverside Park)
        // -------------------------------------------------------------
        PRESET_MAP.put(14, DeThiQ79DTO.builder()
                .tieuDe("Vista City Annual Festival")
                .loaiNoiDung("IMAGE")
                .anhUrl("/images/de-thi/image14.png")
                .tomTatNoiDung("Vista City Annual Festival<br>June 21 at Riverside Park • Tickets: $15 (adults) / $8 (children, 12 and under)<br>• 10:00 A.M.: Face Painting (West Hall)<br>• 11:00 A.M.: Puppet show 'King and I' (Waterfront stage)<br>• Noon: Cooking contest* (East Hall) [*registration required by June 15]<br>• 1:00 P.M.: Concert: Clinton Youth Band (Main stage)<br>• 2:00 P.M.: Tap Dance Performance (Main stage)<br>• 3:00 P.M.: Magic Show: Paul Cash (Main stage)<br>• 4:00 P.M.: Concert: Vista City Orchestra (Waterfront stage)")
                .vanBanThongTin("Vista City Annual Festival\nJune 21 at Riverside Park | Tickets: $15(adults) / $8(children, 12 and under)\n10:00 A.M. | Face Painting | West Hall\n11:00 A.M. | Puppet show \"King and I\" | Waterfront stage\nNoon | Cooking contest* | East Hall (*registration required by June 15)\n1:00 P.M. | Concert: Clinton Youth Band | Main stage\n2:00 P.M. | Tap Dance Performance | Main stage\n3:00 P.M. | Magic Show: Paul Cash | Main stage\n4:00 P.M. | Concert: Vista City Orchestra | Waterfront stage")
                .tinhHuong("Hello, I'm calling for information about the Vista City Annual Festival at Riverside Park on June 21st. Could you answer a few questions for me?")
                .cauHoi1("Where will the festival take place, and how much are admission tickets for children?")
                .thoiGianCau1(15)
                .goiYCau1("The festival will take place at Riverside Park on June 21st, and tickets cost $8 for children aged 12 and under, while adult tickets are $15.")
                .cauHoi2("Can anyone participate in the cooking contest at noon without prior registration?")
                .thoiGianCau2(15)
                .goiYCau2("Actually, that's not correct. The cooking contest in East Hall requires registration in advance by June 15th.")
                .cauHoi3("Could you tell me about all the performances scheduled on the Main stage?")
                .thoiGianCau3(30)
                .goiYCau3("Sure, there are three performances on the Main stage. First, at 1:00 p.m., there is a concert by the Clinton Youth Band. Second, at 2:00 p.m., there is a Tap Dance Performance. Finally, at 3:00 p.m., there is a Magic Show by Paul Cash.")
                .nguonGoc("DE_MAU")
                .deMauId(14)
                .build());

        // -------------------------------------------------------------
        // ĐỀ #15: Palm Island's New Employee Orientation (Slide 38)
        // -------------------------------------------------------------
        PRESET_MAP.put(15, DeThiQ79DTO.builder()
                .tieuDe("Palm Island's New Employee Orientation")
                .loaiNoiDung("IMAGE")
                .anhUrl("/images/de-thi/image15.png")
                .tomTatNoiDung("Palm Island's New Employee Orientation - Monday, November 16th<br>• 08:30~09:30 A.M.: Introduction and morning tea<br>• 09:30~10:30 A.M.: Employee benefits, Erin Morris<br>• 10:30~11:00 A.M.: Demonstration: Resort security procedures (Postponed)<br>• 11:00 A.M.~Noon: Resort tour, Cameron Simmons<br>• Noon~01:00 P.M.: Lunch at resort's restaurant<br>• 01:00~02:00 P.M.: Demonstration: Welcoming new guests, Guest check-in and check-out<br>• 02:00~03:30 P.M.: Workshop on scheduling special events")
                .vanBanThongTin("Palm Island's New Employee Orientation - Monday, November 16th\n08:30~09:30 A.M. | Introduction and morning tea\n09:30~10:30 A.M. | Employee benefits, Erin Morris\n10:30~11:00 A.M. | Demonstration: Resort security procedures (Postponed)\n11:00 A.M.~Noon | Resort tour, Cameron Simmons\nNoon~01:00 P.M. | Lunch at resort's restaurant\n01:00~02:00 P.M. | Demonstration: Welcoming new guests, Guest check-in and check-out\n02:00~03:30 P.M. | Workshop on scheduling special events")
                .tinhHuong("Hello, I'm a new employee starting at Palm Island next Monday. I'd like to ask a few questions about the orientation schedule.")
                .cauHoi1("What time does the new employee orientation begin, and what is scheduled at that time?")
                .thoiGianCau1(15)
                .goiYCau1("The new employee orientation begins at 8:30 a.m., and an introduction and morning tea are scheduled at that time.")
                .cauHoi2("Who will give the sessions on employee benefits and the resort tour?")
                .thoiGianCau2(15)
                .goiYCau2("Erin Morris will give the session on employee benefits from 9:30 to 10:30 a.m., and Cameron Simmons will lead the resort tour from 11:00 a.m. to noon.")
                .cauHoi3("I'm interested in the demonstration of resort security procedures. Could you tell me when it is scheduled?")
                .thoiGianCau3(30)
                .goiYCau3("Actually, the demonstration of resort security procedures has been postponed, so it won't be held. However, there are demonstrations on welcoming new guests and check-in and check-out scheduled from 1:00 to 2:00 p.m.")
                .nguonGoc("DE_MAU")
                .deMauId(15)
                .build());

        // -------------------------------------------------------------
        // ĐỀ #16: [Text 1] Drilling Site Tour Schedule (Slide 10, 15, 33)
        // -------------------------------------------------------------
        PRESET_MAP.put(16, DeThiQ79DTO.builder()
                .tieuDe("[Text 1] Drilling Site Tour Schedule")
                .loaiNoiDung("IMAGE")
                .anhUrl("/images/de-thi/image16.png")
                .tomTatNoiDung("Drilling Site Tour Schedule: Daily at 11:00 a.m. No tours on weekends. Safety equipment required for all participants! 10:45 a.m. Meet at tunnel entrance; 11:00-12:00 Walking tour of finished tunnel with guide; 12:00-1:00 Lunch in underground break room; 1:00-1:30 Talk about drill site safety; 1:30-2:30 Open viewing of drill area (guide available); 3:00 Return to base camp.")
                .vanBanThongTin("Drilling Site Tour Schedule: Daily at 11:00 a.m. No tours on weekends.\nSafety equipment required for all participants!\n10:45 a.m. | Meet at tunnel entrance\n11:00 - 12:00 | Walking tour of finished tunnel with guide\n12:00 - 1:00 | Lunch in underground break room\n1:00 - 1:30 | Talk about drill site safety\n1:30 - 2:30 | Open viewing of drill area (guide available)\n3:00 | Return to base camp")
                .tinhHuong("Hello, this is Mark. I'm calling to ask for some details about the drilling site tour scheduled for tomorrow. Could you please answer a few questions for me?")
                .cauHoi1("What time do we need to meet for the tour?")
                .thoiGianCau1(15)
                .goiYCau1("You need to meet at 10:45 a.m. at the tunnel entrance before the tour begins at 11:00 a.m.")
                .cauHoi2("I heard that tours are also available on weekends. Is that correct?")
                .thoiGianCau2(15)
                .goiYCau2("No, I'm sorry, but that's not correct. There are no tours on weekends; tours are only conducted daily from Monday through Friday.")
                .cauHoi3("Could you please tell me how long the tour lasts and what we will see during the tour?")
                .thoiGianCau3(30)
                .goiYCau3("Certainly. The tour lasts about 4 hours, from 10:45 a.m. until 3:00 p.m. First, from 11:00 to 12:00, you will take a walking tour of the finished part of the tunnel with a guide. Then, after lunch, from 1:30 to 2:30, you will have an open viewing of the drill area with a guide available before returning to base camp at 3:00 p.m.")
                .nguonGoc("DE_MAU")
                .deMauId(16)
                .build());

        // -------------------------------------------------------------
        // ĐỀ #17: [Text 2] International Writers Conference (Slide 11, 17, 35)
        // -------------------------------------------------------------
        PRESET_MAP.put(17, DeThiQ79DTO.builder()
                .tieuDe("[Text 2] International Writers Conference")
                .loaiNoiDung("IMAGE")
                .anhUrl("/images/de-thi/image17.png")
                .tomTatNoiDung("International Writers Conference - Tuesday, March 15, 10:00 a.m. to 6:00 p.m. at Carver Hall, Thorpe Center, West University Campus. Guest Speakers: Jenny Hill (President, Freelance Writers League, 10:00 a.m., Room 17), Marlon Thomson (Publishing Manager, Horton Publishing, 1:00 p.m., Room 21), Angela Moeller (CEO, Editorial Advisory Group, 3:00 p.m., Room 12). Publisher exhibits: 10:00 a.m. to 6:00 p.m., Carver Reception Hall. Open forum: 4:00 p.m. to 6:00 p.m., Room 2. Registration Cost: $26 per person by March 13.")
                .vanBanThongTin("International Writers Conference - Tuesday, March 15, 10:00 a.m. to 6:00 p.m.\nCarver Hall, Thorpe Center, West University Campus\nGuest Speakers:\n- Jenny Hill (President, Freelance Writers League, 10:00 a.m., Room 17)\n- Marlon Thomson (Publishing Manager, Horton Publishing, 1:00 p.m., Room 21)\n- Angela Moeller (CEO, Editorial Advisory Group, 3:00 p.m., Room 12)\nPublisher exhibits: 10:00 a.m. to 6:00 p.m., Carver Reception Hall\nOpen forum: 4:00 p.m. to 6:00 p.m., Room 2\nRegistration Cost: $26 per person by March 13")
                .tinhHuong("Hi, I'm planning to attend the International Writers Conference on March 15th, and I have a couple of questions about the schedule.")
                .cauHoi1("Can you please tell me who Angela Moeller is and where she will be speaking?")
                .thoiGianCau1(15)
                .goiYCau1("Angela Moeller is the CEO of the Editorial Advisory Group, and she will be speaking at 3:00 p.m. in Room 12.")
                .cauHoi2("What can you do at the publisher exhibits?")
                .thoiGianCau2(15)
                .goiYCau2("At the publisher exhibits, you can browse through booths offering valuable information on how to get published, learn what's new in the field, and find out where to send your work.")
                .cauHoi3("Could you tell me about all the guest speakers and when they will be speaking?")
                .thoiGianCau3(30)
                .goiYCau3("Sure, there are three guest speakers scheduled. First, Jenny Hill, President of Freelance Writers League, will speak at 10:00 a.m. in Room 17. Second, Marlon Thomson, Publishing Manager at Horton Publishing, will speak at 1:00 p.m. in Room 21. Finally, Angela Moeller, CEO of Editorial Advisory Group, will speak at 3:00 p.m. in Room 12.")
                .nguonGoc("DE_MAU")
                .deMauId(17)
                .build());

        // -------------------------------------------------------------
        // ĐỀ #18: [Text 3] Southeast Delegation Tour Itinerary (Slide 12, 19, 37)
        // -------------------------------------------------------------
        PRESET_MAP.put(18, DeThiQ79DTO.builder()
                .tieuDe("[Text 3] Southeast Delegation Tour Itinerary")
                .loaiNoiDung("IMAGE")
                .anhUrl("/images/de-thi/image18.png")
                .tomTatNoiDung("Itinerary for Southeast Delegation: 7:00 a.m. Arrive at New York La Guardia Airport on Flight 681, pick-up by Secretary Sullivan; 8:15 a.m. Hotel Compton check-in, 1 hour free; 9:15 a.m. Leave for Government Center, arrive 9:45 a.m.; 10:00 a.m. Meet and greet, Webber Room; 10:30 a.m. Presentation: Global Environmental Issues; 11:30 a.m. Taxi to North Surfside restaurant; 12:00 p.m. Lunch with CEO, HCG Inc.; 1:45 p.m. Taxi to Government Center; 2:30 p.m. Presentation: Solar Energy; 4:00 p.m. Depart for airport with Secretary Sullivan; 7:00 p.m. Flight 682 for Los Angeles.")
                .vanBanThongTin("Itinerary for Southeast Delegation:\n7:00 a.m. | Arrive at New York La Guardia Airport on Flight 681, pick-up by Secretary Sullivan\n8:15 a.m. | Hotel Compton check-in, 1 hour free\n9:15 a.m. | Leave for Government Center, arrive 9:45 a.m.\n10:00 a.m. | Meet and greet, Webber Room\n10:30 a.m. | Presentation: Global Environmental Issues\n11:30 a.m. | Taxi to North Surfside restaurant\n12:00 p.m. | Lunch with CEO, HCG Inc.\n1:45 p.m. | Taxi to Government Center\n2:30 p.m. | Presentation: Solar Energy\n4:00 p.m. | Depart for airport with Secretary Sullivan\n7:00 p.m. | Flight 682 for Los Angeles")
                .tinhHuong("Hello, this is Mr. Gibson. I'm checking on the Southeast delegation's schedule in New York today, and I have a few questions.")
                .cauHoi1("This is Mr. Gibson. Who's taking the Southeast delegation to the airport, and what time is the flight?")
                .thoiGianCau1(15)
                .goiYCau1("Secretary Sullivan is taking the delegation to the airport at 4:00 p.m., and their flight departs at 7:00 p.m. on Flight 682 for Los Angeles.")
                .cauHoi2("What is the delegation doing between presentations?")
                .thoiGianCau2(15)
                .goiYCau2("Between presentations, from 11:30 a.m. to 1:45 p.m., the delegation will take a taxi to North Surfside restaurant to have lunch with the CEO of HCG Inc., and then return by taxi to Government Center.")
                .cauHoi3("Could you please tell me the complete schedule of the delegation for the morning from arrival until their first presentation?")
                .thoiGianCau3(30)
                .goiYCau3("Certainly. First, they arrive at New York La Guardia Airport at 7:00 a.m. and are picked up by Secretary Sullivan. Then, they arrive at Hotel Compton at 8:15 a.m. for check-in with one hour of free time. Next, they leave for Government Center at 9:15 a.m., arriving at 9:45 a.m. Finally, they have a meet and greet at 10:00 a.m. in the Webber Room before their first presentation at 10:30 a.m.")
                .nguonGoc("DE_MAU")
                .deMauId(18)
                .build());

        // -------------------------------------------------------------
        // ĐỀ #19: [Sample Exam] Danville City Tours (Slide 5-6)
        // -------------------------------------------------------------
        PRESET_MAP.put(19, DeThiQ79DTO.builder()
                .tieuDe("[Sample Exam] Danville City Tours")
                .loaiNoiDung("IMAGE")
                .anhUrl("/images/de-thi/image19.png")
                .tomTatNoiDung("Danville City Tours<br>All tours leave from the front of the Piedmont Hotel. Reservations must be made in advance by calling the Tour Office at 593-555-9694. Cost: $75 (adults), $50 (children under 12, must be accompanied by an adult).<br>• 10:00 - Bus leaves from the main entrance of the hotel<br>• 10:00-11:00 - Bus tour of downtown Danville<br>• 11:00 - Arrive at Danville Museum of History<br>• 11:00-1:00 - Guided museum tour<br>• 1:00-2:00 - Lunch at the museum café<br>• 2:00-3:30 - Walking tour of Danville City Park and Gardens<br>• 3:30-5:00 - Bus tour of Danville waterfront<br>• 5:15 - Arrive back at the Piedmont Hotel<br>• 6:00 - Optional dinner at the hotel* (*Must be reserved and paid for when you book your tour tickets. Add $25 per person to the cost of your tour ticket.)")
                .vanBanThongTin("Danville City Tours\nAll tours leave from the front of the Piedmont Hotel. Reservations must be made in advance by calling the Tour Office at 593-555-9694.\nCost: $75 (adults), $50 (children under 12, must be accompanied by an adult)\n• 10:00 - Bus leaves from the main entrance of the hotel\n• 10:00-11:00 - Bus tour of downtown Danville\n• 11:00 - Arrive at Danville Museum of History\n• 11:00-1:00 - Guided museum tour\n• 1:00-2:00 - Lunch at the museum café\n• 2:00-3:30 - Walking tour of Danville City Park and Gardens\n• 3:30-5:00 - Bus tour of Danville waterfront\n• 5:15 - Arrive back at the Piedmont Hotel\n• 6:00 - Optional dinner at the hotel* (*Add $25 per person)")
                .tinhHuong("Hello. I'm interested in taking a tour of Danville, but I'm afraid it might be a bit too expensive. Could you please answer a few questions for me?")
                .cauHoi1("Can you tell me how much it costs to take the tour?")
                .thoiGianCau1(15)
                .goiYCau1("Sure. Let me check the information on the schedule. The tour costs 75 dollars for adults, and for children under 12, the cost is 50 dollars.")
                .cauHoi2("I heard that the tour includes dinner as well as lunch. Is that correct?")
                .thoiGianCau2(15)
                .goiYCau2("Let's see. According to the schedule, there's an optional dinner at the end of the tour. This costs an extra 25 dollars over and above the cost of your tour ticket.")
                .cauHoi3("Does the tour take place mostly in the morning, or will we also visit some places after lunch?")
                .thoiGianCau3(30)
                .goiYCau3("Yes, the tour includes visits to several places after lunch. First, there's a walking tour of Danville City Park and Gardens, which begins at two o'clock. Then after that, at three thirty, the tour goes by bus to the Danville waterfront. Then you'll get back to the hotel by five fifteen.")
                .nguonGoc("DE_MAU")
                .deMauId(19)
                .build());
    }

    public static DeThiQ79DTO getDeMau(int id) {
        return PRESET_MAP.get(id);
    }

    public static boolean hasDeMau(int id) {
        return PRESET_MAP.containsKey(id);
    }
}
