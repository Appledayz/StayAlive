package com.stay.alive.accommodation.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.stay.alive.accommodation.mapper.AccommodationMapper;
import com.stay.alive.accommodation.vo.Accommodation;
import com.stay.alive.common.PageMaker;
import com.stay.alive.common.PageMakerService;
import com.stay.alive.file.ImageFile;
import com.stay.alive.file.mapper.ImageFileMapper;
import com.stay.alive.guestroom.mapper.GuestRoomMapper;
import com.stay.alive.member.group.mapper.MemberGroupMapper;
import com.stay.alive.member.group.vo.MemberGroup;
import com.stay.alive.member.mapper.MemberMapper;
import com.stay.alive.member.vo.Member;

@Service
@Transactional
public class AccommodationService {
	@Autowired
	private AccommodationMapper accommodationMapper;
	@Autowired
	private ImageFileMapper imageFileMapper;
	//회원ID를 통해 숙소정보를 얻어오는 메서드
	@Autowired 
	private MemberGroupMapper memberGroupMapper;
	@Autowired
	private MemberMapper memberMapper;
	@Autowired
	private GuestRoomMapper guestRoomMapper;
	public String[] getAccommodationNames(String memberId) {
		return accommodationMapper.selectAccommodationName(memberId);
	}
	public Accommodation getAccommodationInfo(String name) {
		return accommodationMapper.selectAccommodationInfo(name);
	}
	public Accommodation getAccommodationFromNo(int accommodationNo) {
		return accommodationMapper.selectAccommodationFromNo(accommodationNo);
	}
	public ArrayList<Accommodation> getAccommodationManagementList() {
		return accommodationMapper.selectAccommodationManagementList();
	}
	//숙소 리스트 
	public ArrayList<Accommodation> getAccommodationList(PageMaker pageMaker) {
		pageMaker.setAllCount(accommodationMapper.selectAccommodationCount());
		PageMakerService.pageMakerService(pageMaker);
		return accommodationMapper.selectAccommodationList(pageMaker);
	}
	
	//숙소 수정
	public void modifyAccommodation(Accommodation accommodation, String path) {
		MultipartFile businessNumberFile = accommodation.getBusinessNumberFile();
		if(!businessNumberFile.isEmpty()) { //파일을 선택했다면 기존에 있던 사업자 등록파일을 삭제하고 새로운 파일로 수정
			removeBusinessImageFile(accommodation.getImageFileNo());
			int newFileNo = addBusinessImageFiles(accommodation.getBusinessNumberFile(), path, accommodation.getMemberId());//새로운 사업자 등록 파일 추가
			accommodation.setImageFileNo(newFileNo); //새로 추가된  사업자 등록 번호 세팅 
		}
		accommodationMapper.updateAccommodation(accommodation);
	}
	//숙소 추가
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void addAccommodation(Accommodation accommodation, String path) {
		String sidoName = accommodation.getAddressSidoName();
		String sigunguName = accommodation.getAddressSigunguName();
		if(!accommodationMapper.selectSidoName(sidoName)) { //시도 이름이 데이터베이스에 없으면 추가
			accommodationMapper.insertSidoName(sidoName);
		}
		if(!accommodationMapper.selectSigunguName(sigunguName)) {//시군구 이름이 데이터베이스에 없으면 추가
			accommodationMapper.insertSigunguName(sigunguName);
		}
		int imageFileNo = addBusinessImageFiles(accommodation.getBusinessNumberFile(), path, accommodation.getMemberId());
		accommodation.setImageFileNo(imageFileNo); //사업자 등록증 파일번호 세팅
		int categoryNo = accommodation.getAccommodationCategoryNo();
		String categoryName = accommodationMapper.selectCategoryName(categoryNo); //카테고리 번호를통해 이름을 가져옴
		accommodation.setAccommodationCategoryName(categoryName); //카테고리 이름 데이터베이스에서 가져와서 세팅
		accommodationMapper.insertAccommodation(accommodation); //숙소 등록	
	}
	//사업자 등록 파일 삭제
	public void removeBusinessImageFile(int imageFileNo) {
		ImageFile imageFile = imageFileMapper.selectImageFile(imageFileNo);
		imageFileMapper.deleteImageFile(imageFileNo); //데이터베이스 -> 이미지파일 정보 삭제
		String path =imageFile.getImageFilePath();
		String ext = imageFile.getImageFileExt();
		String stordName = imageFile.getImageFileStoredName();
		File file = new File(path + "/" + stordName + "." + ext);
		System.out.println(path + "/" + stordName + "." + ext);
		file.delete(); //실제 저장된 파일 삭제
	}
	
	private String addImageFile(ImageFile imageFile, MultipartFile file, String path, String memberId, int ImageFileCategoryNo, String ImageFileCategoryName) {
		String storedFileName = "";
		String ext = "";
		if(!file.isEmpty()) {
			imageFile.setMemberId(memberId); //회원 id
			imageFile.setImageFilePath(path); //파일 전체 경로
			String realFileName = file.getOriginalFilename(); //파일 (이름 + 확장자)
			int index = realFileName.indexOf(".");
			String fileName = realFileName.substring(0,index); //파일 이름
			imageFile.setImageFileRealName(fileName);
			ext = realFileName.substring(fileName.length()+1, realFileName.length());// 확장자
			imageFile.setImageFileExt(ext);
			imageFile.setImageFileType(file.getContentType());
			imageFile.setImageFileSize(file.getSize() / 1024); //파일 크기(KB)
			storedFileName = UUID.randomUUID().toString(); //저장될 파일 이름(UUID)
			imageFile.setImageFileStoredName(storedFileName);
			imageFile.setImageFileCategoryNo(ImageFileCategoryNo); //파일 테이블 번호(PK)
			imageFile.setImageFileCategoryName(ImageFileCategoryName);
			File folder = new File (path); //폴더 생성을 위한 파일객체
			if(!folder.exists()) {
				folder.mkdirs();
			}
			File storedFile = new File(path + "/" + storedFileName + "." + ext); //실제 저장될 파일객체
			try {
				file.transferTo(storedFile);
			}
			catch(IllegalStateException | IOException e) {
				e.printStackTrace();
			}
		}
		return "<img style=\"width:300px;height:300px\" src=\"/image/accommodation/" + storedFileName + "." + ext + "\">";
	}
	
	public String addDetailImageFiles(MultipartFile[] multipartFile, String path, String memberId) {
		String imageTag = "";
		ImageFile imageFile = new ImageFile();
		imageFile.setMemberId(memberId); //임시로 회원아이디 세팅
		for(MultipartFile file: multipartFile) {
			if(!file.isEmpty()) {
				imageTag = imageTag + addImageFile(imageFile, file, path, memberId, 3, "숙소");
				imageFileMapper.insertImageFile(imageFile);
			}
		}
		return imageTag;
	}
	public int addBusinessImageFiles(MultipartFile file, String path, String memberId) {
		ImageFile imageFile = new ImageFile();
		addImageFile(imageFile, file, path, memberId, 6, "사업자등록");
		imageFileMapper.insertImageFile(imageFile);
		return imageFile.getImageFileNo();
	}
	public void accommodationRecognitionModify(int accommodationNo) {
		accommodationMapper.updateAccommodationRecognition(accommodationNo); // 숙소 승인 유무 변경(Y)
		Accommodation accommodation = accommodationMapper.selectAccommodationFromNo(accommodationNo);
		String memberId = accommodation.getMemberId();
		Member member = new Member();
		MemberGroup memberGroup = memberGroupMapper.selectOneMemberGroup(5);
		member.setMemberId(memberId);
		member.setGroupNo(memberGroup.getGroupNo());
		member.setGroupName(memberGroup.getGroupName());
		if(memberMapper.updateGroupOfMember(member) == 1) { // 회원그룹 변경(5 - 호스트(진))
			System.out.println("회원그룹 업데이트 성공");
		} else {
			System.out.println("회원그룹 업데이트 실패");
		}
	}
	public ArrayList<Accommodation> getAccommodationSearchList(PageMaker pageMaker, String searchKey, String searchWord) {
		pageMaker.setAllCount(accommodationMapper.selectAccommodationSearchCount(searchKey, searchWord));
		PageMakerService.pageMakerService(pageMaker);
		return accommodationMapper.selectAccommodationSearchList(pageMaker, searchKey, searchWord);
	}
	public int getAccommodationCount() {
		return accommodationMapper.selectAccommodationCount();
	}
	public int getGuestroomCount() {
		return guestRoomMapper.selectGuestroomCount();
	}

}
