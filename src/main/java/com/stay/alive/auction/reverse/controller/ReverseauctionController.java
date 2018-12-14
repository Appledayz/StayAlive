package com.stay.alive.auction.reverse.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.stay.alive.auction.reverse.service.ReverseauctionService;
import com.stay.alive.auction.reverse.vo.Reverseauction;
import com.stay.alive.auction.reverse.vo.ReverseauctionTender;
import com.stay.alive.common.PageMaker;

@Controller
public class ReverseauctionController {
	@Autowired
	private ReverseauctionService reverseauctionService;
	private PageMaker pageMaker;
	
	// 1. 역경매목록 조회
	@GetMapping("reverseauctionList")
	public String reverseauctionList(Model model, @RequestParam(value="page", defaultValue="1")int page) {
		System.out.println("ReverseauctionController.reverseauctionList() GET");
		pageMaker = new PageMaker();
		model.addAttribute("list", reverseauctionService.getReverseauctionList(page, pageMaker));
		model.addAttribute("PM", pageMaker);
		return "/reverseauction/reverseauctionList";
	}
	// 2. 역경매목록 검색
	@GetMapping("reverseauctionSearchList")
	public String reverseauctionSearchList(Model model,@RequestParam HashMap<String, String> paraMap) {
		System.out.println("ReverseauctionController.reverseauctionList() GET");
		String sk = paraMap.get("sk");
		String sv = paraMap.get("sv");
		model.addAttribute("list", reverseauctionService.getReverseauctionSearchList(sk, sv));
		return "/reverseauction/reverseauctionList";
	}
	// 3. 역경매 등록 폼
	@GetMapping("addReverseauction")
	public String addReverseauction() {
		System.out.println("ReverseauctionController.addReverseauction() GET");
		return "/reverseauction/addReverseauction";
	}
	// 4. 역경매 등록 액션
	@PostMapping("addReverseauction")
	public String addReverseauction(Reverseauction reverseauction) {
		System.out.println("ReverseauctionController.addReverseauction() POST");
		reverseauctionService.addReverseauctionOne(reverseauction);
		return "redirect:/reverseauctionList";
	}
	// 5. 역경매 상세 조회
	@GetMapping("reverseauctionDetail")
	public String reverseauctionDetail(Model model, int reverseauctionNo) {
		System.out.println("ReverseauctionController.reverseauctionDetail() GET");
		model.addAttribute("m",reverseauctionService.getReverseauctionOne(reverseauctionNo));
		model.addAttribute("m2", reverseauctionService.getTenderListForOneReverseauction(reverseauctionNo));
		model.addAttribute("m3", reverseauctionService.getReverseauctionSuccessfulbid(reverseauctionNo));
		return "/reverseauction/reverseauctionDetail";
	}
	// 6. 역경매 수정 폼
	@GetMapping("modifyReverseauction")
	public String modifyReverseauction(Model model, int reverseauctionNo) {
		System.out.println("ReverseauctionController.modifyReverseauction() GET");
		model.addAttribute("Reverseauction",reverseauctionService.modifyReverseauctionForm(reverseauctionNo));
		return "/reverseauction/modifyReverseauction";
	}
	// 7. 역경매 수정 액션
	@PostMapping("modifyReverseauction")
	public String modifyReverseauction(Reverseauction reverseauction) {
		System.out.println("ReverseauctionController.modifyReverseauction() POST");
		reverseauctionService.modifyReverseauctionAction(reverseauction);
		return "redirect:/reverseauctionDetail?reverseauctionNo="+reverseauction.getReverseauctionNo();
	}
	// 8. 역경매 삭제
	@GetMapping("removeReverseauction")
	public String deleteReverseauction(int reverseauctionNo) {
		System.out.println("ReverseauctionController.deleteReverseauction() GET");
		System.out.println(reverseauctionService.removeReverseauction(reverseauctionNo));
		return "redirect:/reverseauctionList";
	}
	// 9. 역경매 입찰 상세 조회 (역경매 내 조회)
	@GetMapping("reverseauctionTenderDetail")
	public String reverseauctionTenderDetail(int reverseauctionTenderNo) {
		reverseauctionService.getTenderDetail(reverseauctionTenderNo);
		return "/reverseauction/reverseauctionTenderDetail";
	}
	// 10. 역경매 입찰 등록 폼
	@GetMapping("addReverseauctionTender")
	public String addReverseauctionTender(ReverseauctionTender reverseauctionTender) {
		System.out.println("ReverseauctionController.addReverseauctionTender() GET");
		return "/reverseauction/addReverseauctionTender";
	}
	// 11. 역경매 입찰 등록 액션
	@PostMapping("addReverseauctionTender")
	public String addReverseauctionTenderAction(ReverseauctionTender reverseauctionTender) {
		reverseauctionService.addReverseauctionTender(reverseauctionTender);
		return "redirect:/reverseauctionDetail?reverseauctionNo="+reverseauctionTender.getReverseauctionNo();
	}
	// 12. 역경매 입찰 수정 폼
	@GetMapping("modifyReverseauctionTender")
	public String modifyReverseauctionTender() {
		System.out.println("ReverseauctionController.modifyReverseauctionTender() GET");
		return "/reverseauction/modifyReverseauctionTender";
	}
	// 13. 역경매 입찰 수정 액션
	@PostMapping("modifyReverseauctionTender")
	public String modifyReverseauctionTender(ReverseauctionTender reverseauctionTender) {
		System.out.println("ReverseauctionController.modifyReverseauctionTender() POST");
		return "redirect:/reverseauctionDetail?reverseauctionNo="+reverseauctionTender.getReverseauctionNo();
	}
	// 14. 역경매 입찰 삭제
	@GetMapping("removeReverseauctionTender")
	public String deleteReverseauctionTender(int reverseauctionTenderNo, int reverseauctionNo) {
		System.out.println("ReverseauctionController.deleteReverseauctionTender() GET");
		reverseauctionService.removeReverseauctionTender(reverseauctionTenderNo);
		return "redirect:/reverseauctionDetail?reverseauctionNo="+reverseauctionNo;
	}
	// 15. 낙찰 등록
	@GetMapping("addReverseauctionSuccessfulbid")
	public String addReverseauctionSuccessfulbid(int reverseauctionTenderNo, int reverseauctionNo) {
		System.out.println("ReverseauctionController.addReverseauctionSuccessfulbid() GET");
		reverseauctionService.addReverseauctionSuccessfulbid(reverseauctionTenderNo);
		return "redirect:/reverseauctionDetail?reverseauctionNo="+reverseauctionNo;
	}
	// 16. 낙찰 삭제
	@GetMapping("removeReverseauctionSuccessfulbid")
	public String removeReverseauctionSuccessfulbid(int reverseauctionNo, int reverseauctionSuccessfulbidNo) {
		System.out.println("ReverseauctionController.removeReverseauctionSuccessfulbid() GET");
		reverseauctionService.removeReverseauctionSuccessfulbid(reverseauctionSuccessfulbidNo);
		return "redirect:/reverseauctionDetail?reverseauctionNo="+reverseauctionNo;
	}
}
